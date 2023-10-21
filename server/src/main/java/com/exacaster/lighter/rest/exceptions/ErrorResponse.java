package com.exacaster.lighter.rest.exceptions;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.server.exceptions.response.Error;

import java.net.URI;
import java.util.List;

@Introspected
public class ErrorResponse {
    private final String message;
    private final URI path;
    private final List<Error> errors;

    public ErrorResponse(String message, URI path, List<Error> errors) {
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public ErrorResponse(String message, URI path) {
        this(message, path, List.of());
    }


    public String getMessage() {
        return message;
    }

    public URI getPath() {
        return path;
    }

    public List<Error> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                ", path=" + path +
                ", errors=" + errors +
                '}';
    }
}
