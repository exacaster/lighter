package com.exacaster.lighter.batch;

import java.util.Arrays;

public enum BatchState {
    NOT_STARTED,
    STARTING,
    IDLE,
    BUSY,
    SHUTTING_DOWN,
    ERROR,
    DEAD(true),
    KILLED(true),
    SUCCESS(true);

    private static final BatchState[] INCOMPLETE_STATES = Arrays.stream(values()).filter(val -> !val.isComplete).toArray(BatchState[]::new);

    private final boolean isComplete;

    BatchState(boolean isComplete) {
        this.isComplete = isComplete;
    }

    BatchState() {
        this(false);
    }

    public static BatchState[] incompleteStates() {
        return INCOMPLETE_STATES;
    }

    public boolean isComplete() {
        return isComplete;
    }
}
