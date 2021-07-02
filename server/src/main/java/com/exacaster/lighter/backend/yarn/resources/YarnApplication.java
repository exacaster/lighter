package com.exacaster.lighter.backend.yarn.resources;

public class YarnApplication {
    private final String id;
    private final String trackingUrl;

    public YarnApplication(String id, String trackingUrl) {
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
