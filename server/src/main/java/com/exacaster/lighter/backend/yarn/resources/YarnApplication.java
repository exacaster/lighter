package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

@Introspected
public class YarnApplication {
    private final String id;
    private final String trackingUrl;

    @JsonCreator
    public YarnApplication(@Nullable @JsonProperty String id, @Nullable @JsonProperty String trackingUrl) {
        this.id = id;
        this.trackingUrl = trackingUrl;
    }

    public String getId() {
        return id;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }
}
