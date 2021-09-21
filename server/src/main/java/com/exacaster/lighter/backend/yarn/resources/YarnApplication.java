package com.exacaster.lighter.backend.yarn.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

@Introspected
@JsonIgnoreProperties(ignoreUnknown = true)
public class YarnApplication {
    private final String id;
    private final String trackingUrl;
    private final String finalStatus;

    @JsonCreator
    public YarnApplication(
            @Nullable @JsonProperty("id") String id,
            @Nullable @JsonProperty("trackingUrl") String trackingUrl,
            @Nullable @JsonProperty("finalStatus") String finalStatus) {
        this.id = id;
        this.trackingUrl = trackingUrl;
        this.finalStatus = finalStatus;
    }

    public String getId() {
        return id;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    @Override
    public String toString() {
        return "YarnApplication{" +
                "id='" + id + '\'' +
                ", trackingUrl='" + trackingUrl + '\'' +
                ", finalStatus='" + finalStatus + '\'' +
                '}';
    }
}
