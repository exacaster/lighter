package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.sessions.processors.Output;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import java.util.StringJoiner;

@Introspected
public class Statement {
    private final String id;
    private final String code;
    private final Output output;
    private final String state;

    public Statement(@Nullable String id, String code, @Nullable Output output, @Nullable String state) {
        this.id = id;
        this.code = code;
        this.output = output;
        this.state = state;
    }

    public Statement withIdAndState(String id, String state) {
        return new Statement(id, this.code, this.output, state);
    }

    public Statement withStateAndOutput(String state, Output output) {
        return new Statement(this.id, this.code, output, state);
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Output getOutput() {
        return output;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Statement.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("code='" + code + "'")
                .add("output=" + output)
                .add("state='" + state + "'")
                .toString();
    }
}
