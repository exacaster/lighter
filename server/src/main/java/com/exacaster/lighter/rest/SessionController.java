package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationList;
import com.exacaster.lighter.application.sessions.SessionService;
import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.application.sessions.StatementList;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.rest.exceptions.ErrorResponse;
import com.exacaster.lighter.rest.magic.SessionList;
import com.exacaster.lighter.rest.magic.SparkMagicCompatibility;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@Tags(@Tag(name = "Sessions"))
@Controller("/lighter/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final LogService logService;
    private final SparkMagicCompatibility magicCompatibility;

    public SessionController(SessionService sessionService, LogService logService, SparkMagicCompatibility magicCompatibility) {
        this.sessionService = sessionService;
        this.logService = logService;
        this.magicCompatibility = magicCompatibility;
    }

    @Get
    public Object get(@QueryValue(defaultValue = "0") Integer from,
                      @QueryValue(defaultValue = "100") Integer size, @Nullable @Header("X-Compatibility-Mode") String mode) {
        var sessions = sessionService.fetch(from, size);
        return magicCompatibility.transformOrElse(mode,
                () -> new SessionList(from, sessions.size(), sessions),
                () -> new ApplicationList(from, sessions.size(), sessions)
        );
    }

    @Post
    @Status(HttpStatus.CREATED)
    public Application create(@Valid @Body SessionParams sessionParams) {
        return sessionService.createSession(sessionParams);
    }


    @Put("/{id}")
    @Status(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "session created", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Application.class))}),
            @ApiResponse(responseCode = "409", description = "session with given id already exists", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public Application insertNewSession(@PathVariable String id, @Valid @Body SessionParams sessionParams) {
        return sessionService.createSession(id, sessionParams);
    }

    @Get("/{id}")
    public Optional<Application> get(@PathVariable String id) {
        // Fetch with live state, to make sessions faster
        return sessionService.fetchOne(id, true);
    }

    @Delete("/{id}")
    public void delete(@PathVariable String id) {
        sessionService.deleteOne(id);
    }

    // For backwards compatibility with livy
    @Get("/{id}/state")
    public Optional<Application> getState(@PathVariable String id) {
        return sessionService.fetchOne(id);
    }

    @Get("/{id}/log")
    public Optional<Object> getLog(@PathVariable String id, @Nullable @Header("X-Compatibility-Mode") String mode) {
        return sessionService.fetchOne(id).flatMap(session -> {
            if (session.getState().isComplete()) {
                return logService.fetch(id);
            }

            return logService.fetchLive(session);
        }).map(log -> magicCompatibility.transformOrElse(mode, () -> {
            Map<String, Object> res = new HashMap<>();
            res.put("id", log.getId());
            res.put("log", List.of(log.getLog().split("\n")));
            return res;
        }, () -> log));
    }

    @Post("/{id}/statements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Statement created", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Statement.class))}),
            @ApiResponse(responseCode = "400", description = "Session in invalid state", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Session not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public Optional<Statement> postStatements(@PathVariable String id, @Valid @Body Statement statement) {
        return sessionService.createStatement(id, statement);
    }

    @Get("/{id}/statements")
    public StatementList getStatements(@PathVariable String id,
                                       @QueryValue(defaultValue = "0") Integer from,
                                       @QueryValue(defaultValue = "100") Integer size) {
        var statements = sessionService.getStatements(id, from, size);
        return new StatementList(from, statements);
    }

    @Get("/{id}/statements/{statementId}")
    public Statement getStatement(@PathVariable String id, @PathVariable String statementId) {
        return sessionService.getStatement(id, statementId);
    }

    @Post("/{id}/statements/{statementId}/cancel")
    @Status(HttpStatus.CREATED)
    public Optional<Statement> cancelStatements(@PathVariable String id, @PathVariable String statementId) {
        return sessionService.cancelStatement(id, statementId);
    }
}
