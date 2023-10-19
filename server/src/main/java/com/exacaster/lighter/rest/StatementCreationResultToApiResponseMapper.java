package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.application.sessions.StatementCreationResultMapper;
import io.micronaut.http.HttpResponse;

public class StatementCreationResultToApiResponseMapper implements StatementCreationResultMapper<HttpResponse> {
    @Override
    public HttpResponse mapStatementCreated(Statement sessionCreated) {
        return HttpResponse.created(sessionCreated);
    }

    @Override
    public HttpResponse mapNoSessionExists() {
        return HttpResponse.notFound();
    }

    @Override
    public HttpResponse mapSessionInInvalidState(ApplicationState sessionState) {
        return HttpResponse.badRequest("invalid session state " + sessionState);
    }
}
