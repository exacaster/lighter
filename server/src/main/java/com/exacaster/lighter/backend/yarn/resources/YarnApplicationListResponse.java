package com.exacaster.lighter.backend.yarn.resources;

import java.util.List;

public class YarnApplicationListResponse {
    private final List<YarnApplicationWrapper> apps;

    public YarnApplicationListResponse(List<YarnApplicationWrapper> apps) {
        this.apps = apps;
    }

    public List<YarnApplicationWrapper> getApps() {
        return apps;
    }
}
