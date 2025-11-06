package com.exacaster.lighter.rest.exceptions;

import com.exacaster.lighter.application.sessions.exceptions.SessionLimitExceededException;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = SessionLimitExceededException.class)
public class SessionLimitExceededExceptionHandler implements ExceptionHandler<SessionLimitExceededException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, SessionLimitExceededException exception) {
        return HttpResponse.badRequest(exception.getMessage());
    }
}
