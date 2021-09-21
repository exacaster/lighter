package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import java.util.List;

@Introspected
public class YarnApplicationWrapper {
    private final List<YarnApplication> app;

    @JsonCreator
    public YarnApplicationWrapper(@Nullable @JsonProperty("app") List<YarnApplication> app) {
        this.app = app;
    }

    public List<YarnApplication> getApp() {
        return app;
    }
}
