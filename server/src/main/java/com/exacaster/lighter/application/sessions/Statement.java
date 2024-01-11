package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.sessions.processors.Output;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.StringJoiner;

@Introspected
public class Statement {
    private final String id;
    private final String code;
    private final Output output;
    private final String state;
    private final LocalDateTime createdAt;

    public Statement(@Nullable String id, String code, @Nullable Output output, @Nullable String state, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.output = output;
        this.state = state;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    @NotNull
    public String getCode() {
        return code;
    }

    public Output getOutput() {
        return output;
    }

    public String getState() {
        return state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Statement.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("code='" + code + "'")
                .add("output=" + output)
                .add("state='" + state + "'")
                .add("createdAt='" + createdAt + "'")
                .toString();
    }
}
