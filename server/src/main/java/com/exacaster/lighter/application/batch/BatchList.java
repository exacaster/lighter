package com.exacaster.lighter.application.batch;

import com.exacaster.lighter.application.Application;
import java.util.List;
import java.util.StringJoiner;

public class BatchList {

    private final Integer from;
    private final Integer total;
    private final List<Application> applications;

    public BatchList(Integer from, Integer total, List<Application> applications) {
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
        return new StringJoiner(", ", BatchList.class.getSimpleName() + "[", "]")
                .add("from=" + from)
                .add("total=" + total)
                .add("applications=" + applications)
                .toString();
    }
}
