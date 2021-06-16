package com.exacaster.lighter.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import java.util.StringJoiner;
import javax.annotation.Nullable;

@ConfigurationProperties("lighter")
public class AppConfiguration {
    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer maxRunningJobs;
    private final String sparkHistoryServerUrl;

    @ConfigurationInject
    public AppConfiguration(Integer maxRunningJobs, @Nullable String sparkHistoryServerUrl){
        this.maxRunningJobs = maxRunningJobs;
        this.sparkHistoryServerUrl = sparkHistoryServerUrl;
    }

    public Integer getMaxRunningJobs() {
        return maxRunningJobs;
    }

    public String getSparkHistoryServerUrl() {
        return sparkHistoryServerUrl;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AppConfiguration.class.getSimpleName() + "[", "]")
                .add("maxRunningJobs=" + maxRunningJobs)
                .add("sparkHistoryServerUrl=" + sparkHistoryServerUrl)
                .toString();
    }
}
