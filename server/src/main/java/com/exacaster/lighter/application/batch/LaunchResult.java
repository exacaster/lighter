package com.exacaster.lighter.application.batch;

import com.exacaster.lighter.application.ApplicationState;
import java.util.StringJoiner;

public class LaunchResult {

    private final ApplicationState state;
    private final Exception exception;

    public LaunchResult(ApplicationState state, Exception exception) {
        this.state = state;
        this.exception = exception;
    }

    public ApplicationState getState() {
        return state;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LaunchResult.class.getSimpleName() + "[", "]")
                .add("state=" + state)
                .add("exception=" + exception)
                .toString();
    }
}
