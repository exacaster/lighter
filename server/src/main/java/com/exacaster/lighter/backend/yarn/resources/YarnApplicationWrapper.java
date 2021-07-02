package com.exacaster.lighter.backend.yarn.resources;

import java.util.List;

public class YarnApplicationWrapper {
    private final List<YarnApplication> app;

    public YarnApplicationWrapper(List<YarnApplication> app) {
        this.app = app;
    }

    public List<YarnApplication> getApp() {
        return app;
    }
}
