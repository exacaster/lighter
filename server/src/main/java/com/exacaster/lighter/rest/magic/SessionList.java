package com.exacaster.lighter.rest.magic;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationList;
import java.util.List;
import java.util.StringJoiner;

public class SessionList {

    private final Integer from;
    private final Integer total;
    private final List<Application> sessions;

    public SessionList(Integer from, Integer total, List<Application> sessions) {
        this.from = from;
        this.total = total;
        this.sessions = sessions;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTotal() {
        return total;
    }

    public List<Application> getSessions() {
        return sessions;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApplicationList.class.getSimpleName() + "[", "]")
                .add("from=" + from)
                .add("total=" + total)
                .add("sessions=" + sessions)
                .toString();
    }
}
