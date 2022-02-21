package com.exacaster.lighter.configuration;

import com.exacaster.lighter.spark.SubmitParams;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import java.util.Map;
import java.util.StringJoiner;

@ConfigurationProperties("lighter")
@Introspected
public class AppConfiguration {
    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer maxRunningJobs;
    private final String sparkHistoryServerUrl;
    private final String externalLogsUrlTemplate;
    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer pyGatewayPort;
    @JsonProperty(access = Access.WRITE_ONLY)
    private final String url;
    private final SessionConfiguration sessionConfiguration;

    @ConfigurationInject
    public AppConfiguration(Integer maxRunningJobs, @Nullable String sparkHistoryServerUrl,
            @Nullable String externalLogsUrlTemplate,
            Integer pyGatewayPort, String url, SessionConfiguration sessionConfiguration){
        this.maxRunningJobs = maxRunningJobs;
        this.sparkHistoryServerUrl = sparkHistoryServerUrl;
        this.externalLogsUrlTemplate = externalLogsUrlTemplate;
        this.pyGatewayPort = pyGatewayPort;
        this.url = url;
        this.sessionConfiguration = sessionConfiguration;
    }

    public Integer getMaxRunningJobs() {
        return maxRunningJobs;
    }

    public String getSparkHistoryServerUrl() {
        return sparkHistoryServerUrl;
    }

    public String getExternalLogsUrlTemplate() {
        return externalLogsUrlTemplate;
    }

    public Integer getPyGatewayPort() {
        return pyGatewayPort;
    }

    public String getUrl() {
        return url;
    }

    public SessionConfiguration getSessionConfiguration() {
        return sessionConfiguration;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AppConfiguration.class.getSimpleName() + "[", "]")
                .add("maxRunningJobs=" + maxRunningJobs)
                .add("sparkHistoryServerUrl=" + sparkHistoryServerUrl)
                .add("sessionConfiguration=" + sessionConfiguration)
                .toString();
    }

    @Primary
    @Introspected
    @ConfigurationProperties("session")
    public static class SessionConfiguration {
        private final Integer timeoutMinutes;
        private final Map<String, SubmitParams> permanentSessions;

        @ConfigurationInject
        public SessionConfiguration(@Nullable Integer timeoutMinutes,
                Map<String, SubmitParams> permanentSessions) {
            this.timeoutMinutes = timeoutMinutes;
            this.permanentSessions = permanentSessions;
        }

        public Integer getTimeoutMinutes() {
            return timeoutMinutes;
        }

        public Map<String, SubmitParams> getPermanentSessions() {
            return permanentSessions;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SessionConfiguration.class.getSimpleName() + "[", "]")
                    .add("timeoutMinutes=" + timeoutMinutes)
                    .add("permanentSessions=" + permanentSessions)
                    .toString();
        }
    }
}
