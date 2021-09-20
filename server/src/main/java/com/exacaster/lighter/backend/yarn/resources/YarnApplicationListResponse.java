package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Nullable;

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
