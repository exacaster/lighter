package com.exacaster.lighter.rest.mappers;

import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.application.sessions.StatementCreationResultMapper;
import io.micronaut.http.HttpResponse;

public class StatementCreationResultToApiResponseMapper implements StatementCreationResultMapper<HttpResponse> {

    public static class InvalidSessionStateResponse {
        private final ApplicationState sessionState;

        public InvalidSessionStateResponse(ApplicationState applicationState) {
            this.sessionState = applicationState;
        }

        public ApplicationState getSessionState() {
            return sessionState;
        }
    }

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
        return HttpResponse.badRequest(new InvalidSessionStateResponse(sessionState));
    }
}
