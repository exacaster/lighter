package com.exacaster.lighter.application.sessions.exceptions;

import com.exacaster.lighter.application.ApplicationState;

public class InvalidSessionStateException extends RuntimeException {
    private final ApplicationState sessionState;

    public InvalidSessionStateException(ApplicationState state) {
        super("Invalid session state: " + state);
        this.sessionState = state;
    }

    public ApplicationState getSessionState() {
        return sessionState;
    }

    @Override
    public String toString() {
        return "InvalidSessionStateException{" +
                "sessionState=" + sessionState +
                '}';
    }
}
