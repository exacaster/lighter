package com.exacaster.lighter.batch;

import java.util.Arrays;
import java.util.List;

public enum BatchState {
    not_started,
    starting,
    idle,
    busy,
    shutting_down,
    error,
    dead(true),
    killed(true),
    success(true);

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
}
