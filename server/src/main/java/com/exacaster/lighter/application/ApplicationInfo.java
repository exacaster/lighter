package com.exacaster.lighter.application;

import java.util.StringJoiner;

public class ApplicationInfo {

    private final ApplicationState state;
    private final String applicationId;

    public ApplicationInfo(ApplicationState state, String applicationId) {
        this.state = state;
        this.applicationId = applicationId;
    }

    public ApplicationState getState() {
        return state;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApplicationInfo.class.getSimpleName() + "[", "]")
                .add("state=" + state)
                .add("applicationId='" + applicationId + "'")
                .toString();
    }
}
