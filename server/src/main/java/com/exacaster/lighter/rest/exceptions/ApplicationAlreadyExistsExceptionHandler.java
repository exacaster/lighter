package com.exacaster.lighter.rest.exceptions;

import com.exacaster.lighter.storage.ApplicationAlreadyExistsException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
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
@Requires(classes = ApplicationAlreadyExistsException.class)
public class ApplicationAlreadyExistsExceptionHandler implements ExceptionHandler<ApplicationAlreadyExistsException, HttpResponse<?>> {

    private final ErrorResponseProcessor<?> responseProcessor;

    @Inject
    public ApplicationAlreadyExistsExceptionHandler(ErrorResponseProcessor<?> responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public HttpResponse<?> handle(HttpRequest request, ApplicationAlreadyExistsException exception) {
        final ErrorContext.Builder contextBuilder = ErrorContext.builder(request).cause(exception);
        MutableHttpResponse<?> response = HttpResponse.status(HttpStatus.CONFLICT);

        return responseProcessor.processResponse(contextBuilder
                .error(new DetailedError(exception.getMessage(), Map.of("sessionId", exception.getApplicationId())))
                .build(), response);
    }
}
