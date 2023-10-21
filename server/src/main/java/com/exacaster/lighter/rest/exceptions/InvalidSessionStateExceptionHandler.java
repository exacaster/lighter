package com.exacaster.lighter.rest.exceptions;

import com.exacaster.lighter.application.sessions.exceptions.InvalidSessionStateException;
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

import java.util.List;
import java.util.Map;

@Produces
@Singleton
@Requires(classes = InvalidSessionStateException.class)
public class InvalidSessionStateExceptionHandler implements ExceptionHandler<InvalidSessionStateException, HttpResponse<?>> {

    private final ErrorResponseProcessor<?> responseProcessor;

    @Inject
    public InvalidSessionStateExceptionHandler(ErrorResponseProcessor<?> responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public HttpResponse<?> handle(HttpRequest request, InvalidSessionStateException exception) {
        final ErrorContext.Builder contextBuilder = ErrorContext.builder(request).cause(exception);
        MutableHttpResponse<?> response = HttpResponse.badRequest();

        return responseProcessor.processResponse(contextBuilder
                .error(new DetailedError(exception.getMessage(), Map.of("sessionState", exception.getSessionState())))
                .build(), response);
    }
}
