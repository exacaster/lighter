package com.exacaster.lighter.application;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    private static final List<ApplicationState> RUNNING_STATES = Arrays.stream(values())
            .filter(val -> !val.isComplete && !val.equals(NOT_STARTED))
            .toList();

    private static final List<ApplicationState> FINISHED_STATES = Arrays.stream(values())
            .filter(val -> val.isComplete)
            .toList();

    private final boolean isComplete;

    ApplicationState(boolean isComplete) {
        this.isComplete = isComplete;
    }

    ApplicationState() {
        this(false);
    }

    public static List<ApplicationState> runningStates() {
        return RUNNING_STATES;
    }

    public static List<ApplicationState> finishedStates() {
        return FINISHED_STATES;
    }

    public static Optional<ApplicationState> from(String state) {
        if (state == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(ApplicationState.valueOf(state));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean isComplete() {
        return isComplete;
    }

    @JsonValue
    public String getKey() {
        return name().toLowerCase();
    }
}
