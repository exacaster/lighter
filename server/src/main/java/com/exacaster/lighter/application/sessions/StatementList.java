package com.exacaster.lighter.application.sessions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.StringJoiner;

public class StatementList {

    private final Integer from;
    private final List<Statement> statements;

    @JsonCreator
    public StatementList(@JsonProperty("from") Integer from, @JsonProperty("statements") List<Statement> statements) {
        this.from = from;
        this.statements = statements;
    }

    public Integer getFrom() {
        return from;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StatementList.class.getSimpleName() + "[", "]")
                .add("from=" + from)
                .add("statements=" + statements)
                .toString();
    }
}
