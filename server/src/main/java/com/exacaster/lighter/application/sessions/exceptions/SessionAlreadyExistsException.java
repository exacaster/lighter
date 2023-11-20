package com.exacaster.lighter.application.sessions.exceptions;

public class SessionAlreadyExistsException extends RuntimeException {
    private final String sessionId;

    public SessionAlreadyExistsException(String sessionId) {
        super("Session already exists: " + sessionId);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return "SessionAlreadyExistsException{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }
}
