package com.exacaster.lighter.rest.exceptions;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;

import javax.inject.Singleton;

@Primary
@Singleton
public class ApiErrorResponseProcessor implements ErrorResponseProcessor<ErrorResponse> {
    @NonNull
    @Override
    public MutableHttpResponse<ErrorResponse> processResponse(@NonNull ErrorContext errorContext,
                                                              @NonNull MutableHttpResponse response) {
        if (errorContext.getRequest().getMethod() == HttpMethod.HEAD) {
            return (MutableHttpResponse<ErrorResponse>) response;
        }
        var path = errorContext.getRequest().getUri();
        var error = new ErrorResponse(response.getStatus().getReason(), path, errorContext.getErrors());

        return response.body(error).contentType(MediaType.APPLICATION_JSON_TYPE);
    }
}
