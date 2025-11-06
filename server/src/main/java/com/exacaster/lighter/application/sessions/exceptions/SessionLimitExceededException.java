package com.exacaster.lighter.application.sessions.exceptions;

public class SessionLimitExceededException extends RuntimeException {

    private final Integer maxRunning;

    public SessionLimitExceededException(Integer maxRunning) {
        super(String.format("Maximum number of running sessions (%d) has been reached", maxRunning));
        this.maxRunning = maxRunning;
    }

    public Integer getMaxRunning() {
        return maxRunning;
    }
}
