package com.exacaster.lighter.application.sessions.exceptions;

public class SessionLimitExceededException extends RuntimeException {

    public SessionLimitExceededException(String message) {
        super(message);
    }
}
