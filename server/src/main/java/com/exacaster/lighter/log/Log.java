package com.exacaster.lighter.log;

import com.exacaster.lighter.storage.Entity;
import java.time.LocalDateTime;
import java.util.StringJoiner;

public class Log implements Entity {

    private final String id;
    private final String log;

    public Log(String id, String log){
        this.id = id;
        this.log = log;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return LocalDateTime.MIN;
    }

    public String getLog() {
        return log;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Log.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("log='" + log + "'")
                .toString();
    }
}
