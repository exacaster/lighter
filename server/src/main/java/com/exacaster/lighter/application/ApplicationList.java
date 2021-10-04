package com.exacaster.lighter.application;

import io.micronaut.core.annotation.Introspected;

import java.util.List;
import java.util.StringJoiner;

@Introspected
public class ApplicationList {

    private final Integer from;
    private final Integer total;
    private final List<Application> applications;

    public ApplicationList(Integer from, Integer total, List<Application> applications) {
        this.from = from;
        this.total = total;
        this.applications = applications;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTotal() {
        return total;
    }

    public List<Application> getApplications() {
        return applications;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApplicationList.class.getSimpleName() + "[", "]")
                .add("from=" + from)
                .add("total=" + total)
                .add("applications=" + applications)
                .toString();
    }
}
