package com.exacaster.lighter.application.sessions.processors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_TABLE_JSON = "application/vnd.livy.table.v1+json";

    private final Map<String, Object> content;
    private final String error;
    private final String message;
    private final List<String> traceback;

    @JsonCreator
    public Result(@JsonProperty("content") Map<String, Object> content,
            @JsonProperty("error") String error,
            @JsonProperty("message") String message,
            @JsonProperty("traceback") List<String> traceback) {
        this.content = content;
        this.error = error;
        this.message = message;
        this.traceback = traceback;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getTraceback() {
        return traceback;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                .add("content=" + content)
                .add("ename='" + error + "'")
                .add("evalue='" + message + "'")
                .add("traceback=" + traceback)
                .toString();
    }
}
