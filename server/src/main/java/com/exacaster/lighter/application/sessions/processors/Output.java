package com.exacaster.lighter.application.sessions.processors;

import java.util.Map;
import java.util.StringJoiner;

public class Output {
    private final String status;
    private int executionCount;
    private final Map<String, Object> data;
    private final String evalue;
    private final String traceback;

    public Output(String status, int executionCount, Map<String, Object> data, String evalue, String traceback) {
        this.status = status;
        this.executionCount = executionCount;
        this.data = data;
        this.evalue = evalue;
        this.traceback = traceback;
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

    public String getEvalue() {
        return evalue;
    }

    public String getTraceback() {
        return traceback;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Output.class.getSimpleName() + "[", "]")
                .add("status='" + status + "'")
                .add("executionCount=" + executionCount)
                .add("data=" + data)
                .add("evalue='" + evalue + "'")
                .add("traceback='" + traceback + "'")
                .toString();
    }
}
