package com.exacaster.lighter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import java.util.StringJoiner;

@ConfigurationProperties("lighter")
public class AppConfiguration {
    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer maxRunningJobs;
    private final String sparkHistoryServerUrl;
    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer pyGatewayPort;
    @JsonProperty(access = Access.WRITE_ONLY)
    private final String url;
    private final SessionConfiguration sessionConfiguration;

    @ConfigurationInject
    public AppConfiguration(Integer maxRunningJobs, @Nullable String sparkHistoryServerUrl,
            Integer pyGatewayPort, String url, SessionConfiguration sessionConfiguration){
        this.maxRunningJobs = maxRunningJobs;
        this.sparkHistoryServerUrl = sparkHistoryServerUrl;
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

    @ConfigurationProperties("session")
    public static class SessionConfiguration {
        private final Integer timeoutMinutes;

        @ConfigurationInject
        public SessionConfiguration(@Nullable Integer timeoutMinutes) {
            this.timeoutMinutes = timeoutMinutes;
        }

        public Integer getTimeoutMinutes() {
            return timeoutMinutes;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SessionConfiguration.class.getSimpleName() + "[", "]")
                    .add("timeoutMinutes=" + timeoutMinutes)
                    .toString();
        }
    }
}
