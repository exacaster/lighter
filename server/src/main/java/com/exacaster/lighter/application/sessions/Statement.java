package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.sessions.processors.Output;

public class Statement {
    private final String id;
    private final String code;
    private final Output output;
    private final String state;

    public Statement(String id, String code, Output output, String state) {
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
}
