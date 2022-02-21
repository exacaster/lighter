package com.exacaster.lighter.application.sessions.processors;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.sessions.Statement;
import java.util.Optional;

public interface StatementHandler {
    Statement processStatement(String id, Statement statement);
    Statement getStatement(String id, String statementId);
    Optional<Statement> cancelStatement(String id, String statementId);

    boolean hasWaitingStatement(Application application);
}
