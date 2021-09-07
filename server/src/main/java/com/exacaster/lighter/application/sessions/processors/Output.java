package com.exacaster.lighter.application.sessions.processors;

import java.util.Map;

public class Output {
    private final String status;
    private int executionCount;
    private final Map<String, Object> data;

    public Output(String status, int executionCount, Map<String, Object> data) {
        this.status = status;
        this.executionCount = executionCount;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
