package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

@Introspected
public class YarnApplicationResponse {
    private final YarnApplication app;

    @JsonCreator
    public YarnApplicationResponse(@Nullable @JsonProperty("app") YarnApplication app) {
        this.app = app;
    }

    public YarnApplication getApp() {
        return app;
    }
}
