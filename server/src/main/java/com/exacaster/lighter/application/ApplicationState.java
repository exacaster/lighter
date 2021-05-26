package com.exacaster.lighter.application;

import java.util.Arrays;
import java.util.List;

public enum ApplicationState {
    NOT_STARTED,
    STARTING,
    IDLE,
    BUSY,
    SHUTTING_DOWN,
    ERROR,
    DEAD(true),
    KILLED(true),
    SUCCESS(true);

    private static final List<ApplicationState> INCOMPLETE_STATES = Arrays.stream(values()).filter(val -> !val.isComplete).toList();

    private final boolean isComplete;

    ApplicationState(boolean isComplete) {
        this.isComplete = isComplete;
    }

    ApplicationState() {
        this(false);
    }

    public static List<ApplicationState> incompleteStates() {
        return INCOMPLETE_STATES;
    }

    public boolean isComplete() {
        return isComplete;
    }
}
