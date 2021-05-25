package com.exacaster.lighter.storage;

public class InvalidQueryException extends RuntimeException {

    public InvalidQueryException(String message, Exception exception) {
        super(message, exception);
    }
}
