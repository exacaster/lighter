package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationList;
import com.exacaster.lighter.application.sessions.SessionService;
import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.spark.SubmitParams;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;
import java.util.List;
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
    public Application create(@Valid @Body SubmitParams session) {
        return sessionService.createStatement(session);
    }

    @Get("/{id}")
    public Optional<Application> get(@PathVariable String id) {
        return sessionService.fetchOne(id);
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
        return sessionService.fetchOne(id).flatMap(logService::fetchLive);
    }

    @Get("/{id}/statements")
    public List<Statement> getStatements(@PathVariable String id) {
        return sessionService.getStatements(id);
    }

    @Post("/{id}/statements")
    public Statement postStatements(@PathVariable String id, @Valid @Body Statement statement) {
        return sessionService.createStatement(id, statement);
    }

    @Get("/{id}/statements/{statementId}")
    public Statement postStatements(@PathVariable String id, @PathVariable String statementId) {
        return sessionService.getStatement(id, statementId);
    }

    @Post("/{id}/statements/{statementId}/cancel")
    public Statement cancelStatements(@PathVariable String id, @PathVariable String statementId) {
        return sessionService.cancelStatement(id, statementId);
    }

    @Post("/sessions/{id}/completion")
    public List<String> completion(@PathVariable String id) {
        return sessionService.runStatement(id);
    }
}
