package com.exacaster.lighter.application;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ApplicationState {
    NOT_STARTED,
    STARTING,
    IDLE,
    BUSY,
    SHUTTING_DOWN,
    ERROR(true),
    DEAD(true),
    KILLED(true),
    SUCCESS(true);

    private static final List<ApplicationState> INCOMPLETE_STATES = Arrays.stream(values())
            .filter(val -> !val.isComplete)
            .collect(Collectors.toList());

    private static final List<ApplicationState> RUNNING_STATES = Arrays.stream(values())
            .filter(val -> !val.isComplete && !val.equals(NOT_STARTED))
            .collect(Collectors.toList());

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

    public static List<ApplicationState> runningStates() {
        return RUNNING_STATES;
    }

    public boolean isComplete() {
        return isComplete;
    }

    @JsonValue
    public String getKey() {
        return name().toLowerCase();
    }
}
