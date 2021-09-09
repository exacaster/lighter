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
    @JsonProperty(access = Access.WRITE_ONLY)
    private final Integer pyGatewayPort;
    @JsonProperty(access = Access.WRITE_ONLY)
    private final String url;

    @ConfigurationInject
    public AppConfiguration(Integer maxRunningJobs, @Nullable String sparkHistoryServerUrl,
            Integer pyGatewayPort, String url){
        this.maxRunningJobs = maxRunningJobs;
        this.sparkHistoryServerUrl = sparkHistoryServerUrl;
        this.pyGatewayPort = pyGatewayPort;
        this.url = url;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", AppConfiguration.class.getSimpleName() + "[", "]")
                .add("maxRunningJobs=" + maxRunningJobs)
                .add("sparkHistoryServerUrl=" + sparkHistoryServerUrl)
                .toString();
    }
}
