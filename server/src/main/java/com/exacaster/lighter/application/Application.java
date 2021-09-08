package com.exacaster.lighter.application;

import com.exacaster.lighter.spark.SubmitParams;
import com.exacaster.lighter.storage.Entity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

public class Application implements Entity {

    private final String id;
    private final ApplicationType type;
    private final ApplicationState state;
    private final String appId;
    private final String appInfo;
    // For Sparkmagic compatibility
    private final List<String> log = List.of();
    private final SubmitParams submitParams;
    private final LocalDateTime createdAt;
    private final LocalDateTime contactedAt;

    public Application(String id, ApplicationType type, ApplicationState state, String appId, String appInfo,
            SubmitParams submitParams,
            LocalDateTime createdAt, LocalDateTime contactedAt) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.appId = appId;
        this.appInfo = appInfo;
        this.submitParams = submitParams;
        this.createdAt = createdAt;
        this.contactedAt = contactedAt;
    }

    @Override
    public String getId() {
        return id;
    }

    public ApplicationType getType() {
        return type;
    }

    public ApplicationState getState() {
        return state;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppInfo() {
        return appInfo;
    }

    public List<String> getLog() {
        return log;
    }

    public SubmitParams getSubmitParams() {
        return submitParams;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getContactedAt() {
        return contactedAt;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Application.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("type=" + type)
                .add("state=" + state)
                .add("appId='" + appId + "'")
                .add("appInfo='" + appInfo + "'")
                .add("submitParams=" + submitParams)
                .add("createdAt=" + createdAt)
                .add("contactedAt=" + contactedAt)
                .toString();
    }
}
