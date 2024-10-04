package com.exacaster.lighter.configuration;

import static io.micronaut.core.convert.format.MapFormat.MapTransformation.FLAT;
import static io.micronaut.core.naming.conventions.StringConvention.RAW;
import static java.util.Optional.ofNullable;

import com.exacaster.lighter.application.SubmitParams;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.MapFormat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@ConfigurationProperties("lighter")
@Introspected
public class AppConfiguration {

    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer maxRunningJobs;
    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer maxStartingJobs;
    private final String sparkHistoryServerUrl;
    private final String externalLogsUrlTemplate;
    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer pyGatewayPort;
    @JsonProperty(access = Access.WRITE_ONLY)
    private final String url;
    private final Duration zombieInterval;
    private final SessionConfiguration sessionConfiguration;
    private final Map<String, String> batchDefaultConf;
    private final Map<String, String> sessionDefaultConf;

    @ConfigurationInject
    public AppConfiguration(Integer maxRunningJobs,
            Integer maxStartingJobs,
            @Nullable String sparkHistoryServerUrl,
            @Nullable String externalLogsUrlTemplate,
            Integer pyGatewayPort,
            String url,
            Duration zombieInterval,
            SessionConfiguration sessionConfiguration,
            @MapFormat(transformation = FLAT, keyFormat = RAW)
            @Nullable Map<String, String> batchDefaultConf,
            @Nullable Map<String, String> sessionDefaultConf) {
        this.maxRunningJobs = maxRunningJobs;
        this.maxStartingJobs = maxStartingJobs;
        this.sparkHistoryServerUrl = sparkHistoryServerUrl;
        this.externalLogsUrlTemplate = externalLogsUrlTemplate;
        this.pyGatewayPort = pyGatewayPort;
        this.url = url;
        this.zombieInterval = zombieInterval;
        this.sessionConfiguration = sessionConfiguration;
        this.batchDefaultConf = ofNullable(batchDefaultConf).orElse(Map.of());
        this.sessionDefaultConf = ofNullable(sessionDefaultConf).orElse(Map.of());
    }

    public Integer getMaxRunningJobs() {
        return maxRunningJobs;
    }

    public Integer getMaxStartingJobs() {
        return maxStartingJobs;
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

    public Duration getZombieInterval() {
        return zombieInterval;
    }

    public SessionConfiguration getSessionConfiguration() {
        return sessionConfiguration;
    }

    public Map<String, String> getBatchDefaultConf() {
        return batchDefaultConf;
    }

    public Map<String, String> getSessionDefaultConf() {
        return sessionDefaultConf;
    }

    @Override
    public String toString() {
        return "AppConfiguration{" +
                "maxRunningJobs=" + maxRunningJobs +
                ", maxStartingJobs=" + maxStartingJobs +
                ", sparkHistoryServerUrl='" + sparkHistoryServerUrl + '\'' +
                ", externalLogsUrlTemplate='" + externalLogsUrlTemplate + '\'' +
                ", pyGatewayPort=" + pyGatewayPort +
                ", url='" + url + '\'' +
                ", zombieInterval=" + zombieInterval +
                ", sessionConfiguration=" + sessionConfiguration +
                ", batchDefaultConf=" + batchDefaultConf +
                ", sessionDefaultConf=" + sessionDefaultConf +
                '}';
    }

    @Introspected
    public static class PermanentSession {

        private final String id;
        @JsonAlias("submit-params")
        private final SubmitParams submitParams;

        public PermanentSession(String id, SubmitParams submitParams) {
            this.id = id;
            this.submitParams = submitParams;
        }

        public String getId() {
            return id;
        }

        public SubmitParams getSubmitParams() {
            return submitParams;
        }

        @Override
        public String toString() {
            return "PermanentSession{" +
                    "id='" + id + '\'' +
                    ", submitParams=" + submitParams +
                    '}';
        }
    }

    @Primary
    @Introspected
    @ConfigurationProperties("session")
    public static class SessionConfiguration {

        private final Duration timeoutInterval;
        private final Boolean timeoutActive;
        private final List<PermanentSession> permanentSessions;
        private final Duration scheduleInterval;
        private final Duration trackRunningInterval;

        @ConfigurationInject
        public SessionConfiguration(@Nullable Duration timeoutInterval,
                Boolean timeoutActive,
                List<PermanentSession> permanentSessions,
                Duration scheduleInterval,
                Duration trackRunningInterval) {
            this.timeoutInterval = timeoutInterval;
            this.timeoutActive = timeoutActive;
            this.permanentSessions = permanentSessions;
            this.scheduleInterval = scheduleInterval;
            this.trackRunningInterval = trackRunningInterval;
        }

        public Duration getTimeoutInterval() {
            return timeoutInterval;
        }

        public boolean shouldTimeoutActive() {
            return Boolean.TRUE.equals(timeoutActive);
        }

        public List<PermanentSession> getPermanentSessions() {
            return permanentSessions;
        }

        public Duration getScheduleInterval() {
            return scheduleInterval;
        }

        public Duration getTrackRunningInterval() {
            return trackRunningInterval;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SessionConfiguration.class.getSimpleName() + "[", "]")
                    .add("timeoutMinutes=" + timeoutInterval)
                    .add("permanentSessions=" + permanentSessions)
                    .add("scheduleIntervalSeconds=" + scheduleInterval)
                    .add("trackRunningInterval=" + trackRunningInterval)
                    .toString();
        }
    }
}
