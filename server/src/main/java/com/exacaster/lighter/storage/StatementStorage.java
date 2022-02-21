package com.exacaster.lighter.storage;

import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.application.sessions.processors.Output;
import java.util.List;
import java.util.Optional;

public interface StatementStorage {

    List<Statement> find(String sessionId);

    List<Statement> findByState(String sessionId, String state);

    Optional<Statement> updateState(String sessionId, String statementId, String state);

    Optional<Statement> findLatest(String sessionId);

    void update(String sessionId, String statementId, String state, Output output);

    Statement create(String sessionId, Statement statement);
}
