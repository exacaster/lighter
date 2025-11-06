package com.exacaster.lighter.rest.exceptions;

import com.exacaster.lighter.application.sessions.exceptions.SessionLimitExceededException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;

@Produces
@Singleton
@Requires(classes = SessionLimitExceededException.class)
public class SessionLimitExceededExceptionHandler implements ExceptionHandler<SessionLimitExceededException, HttpResponse<?>> {

    private final ErrorResponseProcessor<?> responseProcessor;

    @Inject
    public SessionLimitExceededExceptionHandler(ErrorResponseProcessor<?> responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public HttpResponse<?> handle(HttpRequest request, SessionLimitExceededException exception) {
        final ErrorContext.Builder contextBuilder = ErrorContext.builder(request).cause(exception);
        MutableHttpResponse<?> response = HttpResponse.badRequest();

        return responseProcessor.processResponse(contextBuilder
                .error(new DetailedError(exception.getMessage(), Map.of("maxRunning", exception.getMaxRunning())))
                .build(), response);
    }
}
