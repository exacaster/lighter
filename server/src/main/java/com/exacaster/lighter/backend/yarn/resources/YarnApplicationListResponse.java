package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import java.util.List;

@Introspected
public class YarnApplicationListResponse {
    private final List<YarnApplicationWrapper> apps;

    @JsonCreator
    public YarnApplicationListResponse(@Nullable @JsonProperty List<YarnApplicationWrapper> apps) {
        this.apps = apps;
    }

    public List<YarnApplicationWrapper> getApps() {
        return apps;
    }
}
