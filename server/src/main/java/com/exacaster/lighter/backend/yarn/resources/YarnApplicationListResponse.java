package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

@Introspected
public class YarnApplicationListResponse {
    private final YarnApplicationWrapper apps;

    @JsonCreator
    public YarnApplicationListResponse(@Nullable @JsonProperty("apps") YarnApplicationWrapper apps) {
        this.apps = apps;
    }

    public YarnApplicationWrapper getApps() {
        return apps;
    }
}
