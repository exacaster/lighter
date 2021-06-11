package com.exacaster.lighter.configuration;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import java.util.StringJoiner;

@ConfigurationProperties("lighter")
public class AppConfiguration {
    private final Integer maxRunningJobs;

    @ConfigurationInject
    public AppConfiguration(Integer maxRunningJobs){
        this.maxRunningJobs = maxRunningJobs;
    }

    public Integer getMaxRunningJobs() {
        return maxRunningJobs;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AppConfiguration.class.getSimpleName() + "[", "]")
                .add("maxRunningJobs=" + maxRunningJobs)
                .toString();
    }
}
