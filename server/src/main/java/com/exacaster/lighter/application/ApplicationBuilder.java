package com.exacaster.lighter.application;

import java.time.LocalDateTime;

public class ApplicationBuilder {

    private String id;
    private ApplicationType type;
    private ApplicationState state;
    private String appId;
    private String appInfo;
    private SubmitParams submitParams;
    private LocalDateTime createdAt;
    private LocalDateTime contactedAt;
    private LocalDateTime finishedAt;
    private boolean deleted;

    private ApplicationBuilder() {

    }

    public static ApplicationBuilder builder(Application batch) {
        var builder = new ApplicationBuilder();
        builder.setAppId(batch.getAppId());
        builder.setAppInfo(batch.getAppInfo());
        builder.setSubmitParams(batch.getSubmitParams());
        builder.setState(batch.getState());
        builder.setType(batch.getType());
        builder.setId(batch.getId());
        builder.setCreatedAt(batch.getCreatedAt());
        builder.setContactedAt(batch.getContactedAt());
        builder.setFinishedAt(batch.getFinishedAt());

        return builder;
    }

    public static ApplicationBuilder builder() {
        return new ApplicationBuilder();
    }

    public ApplicationBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public ApplicationBuilder setType(ApplicationType type) {
        this.type = type;
        return this;
    }

    public ApplicationBuilder setState(ApplicationState state) {
        this.state = state;
        return this;
    }

    public ApplicationBuilder setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public ApplicationBuilder setAppInfo(String appInfo) {
        this.appInfo = appInfo;
        return this;
    }

    public ApplicationBuilder setSubmitParams(SubmitParams submitParams) {
        this.submitParams = submitParams;
        return this;
    }

    public ApplicationBuilder setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ApplicationBuilder setContactedAt(LocalDateTime contactedAt) {
        this.contactedAt = contactedAt;
        return this;
    }

    public ApplicationBuilder setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
        return this;
    }

    public ApplicationBuilder setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public Application build() {
        return new Application(id, type, state, appId, appInfo, submitParams, createdAt, contactedAt, finishedAt, deleted);
    }
}