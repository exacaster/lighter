package com.exacaster.lighter.backend.yarn.resources;

public class YarnApplicationResponse {
    private final YarnApplication app;

    public YarnApplicationResponse(YarnApplication app) {
        this.app = app;
    }

    public YarnApplication getApp() {
        return app;
    }
}
