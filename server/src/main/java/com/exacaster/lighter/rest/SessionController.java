package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationList;
import com.exacaster.lighter.application.sessions.SessionService;
import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.spark.SubmitParams;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.validation.Validated;
import java.util.Optional;
import javax.validation.Valid;

@Validated
@Controller("/lighter/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final LogService logService;

    public SessionController(SessionService sessionService, LogService logService) {
        this.sessionService = sessionService;
        this.logService = logService;
    }

    @Get
    public ApplicationList get(@QueryValue(defaultValue = "0") Integer from, @QueryValue(defaultValue = "100") Integer size) {
        var sessions = sessionService.fetch(from, size);
        return new ApplicationList(from, sessions.size(), sessions);
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
    public Optional<Log> getLog(@PathVariable String id) {
        return sessionService.fetchOne(id).flatMap(session -> {
            if (session.getState().isComplete()) {
                return logService.fetch(id);
            }

            return logService.fetchLive(session);
        });
    }

    @Post("/{id}/statements")
    @Status(HttpStatus.CREATED)
    public Statement postStatements(@PathVariable String id, @Valid @Body Statement statement) {
        return sessionService.createSession(id, statement);
    }

    @Get("/{id}/statements/{statementId}")
    public Statement postStatements(@PathVariable String id, @PathVariable String statementId) {
        return sessionService.getStatement(id, statementId);
    }

    @Post("/{id}/statements/{statementId}/cancel")
    @Status(HttpStatus.CREATED)
    public Statement cancelStatements(@PathVariable String id, @PathVariable String statementId) {
        return sessionService.cancelStatement(id, statementId);
    }
}
