package com.exacaster.lighter.backend.yarn.resources;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class State {
    private final String state;

    public State(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
