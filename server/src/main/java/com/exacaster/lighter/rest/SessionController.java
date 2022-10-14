package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationList;
import com.exacaster.lighter.application.sessions.SessionService;
import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.rest.magic.SessionList;
import com.exacaster.lighter.rest.magic.SparkMagicCompatibility;
import com.exacaster.lighter.application.SubmitParams;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.validation.Validated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;

@Validated
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
    public Application create(@Body SubmitParams session) {
        return sessionService.createSession(session);
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
    @Status(HttpStatus.CREATED)
    public Statement postStatements(@PathVariable String id, @Valid @Body Statement statement) {
        return sessionService.createStatement(id, statement);
    }

    @Get("/{id}/statements/{statementId}")
    public Statement getStatements(@PathVariable String id, @PathVariable String statementId) {
        return sessionService.getStatement(id, statementId);
    }

    @Post("/{id}/statements/{statementId}/cancel")
    @Status(HttpStatus.CREATED)
    public Optional<Statement> cancelStatements(@PathVariable String id, @PathVariable String statementId) {
        return sessionService.cancelStatement(id, statementId);
    }
}
