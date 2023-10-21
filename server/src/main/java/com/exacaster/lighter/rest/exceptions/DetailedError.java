package com.exacaster.lighter.rest.exceptions;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.server.exceptions.response.Error;

@Introspected
public class DetailedError implements Error {

    private final String message;

    private final Object details;

    public DetailedError(String message, Object details) {
        this.message = message;
        this.details = details;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Object getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "DetailedError{" +
                "message='" + message + '\'' +
                ", details=" + details +
                '}';
    }
}
