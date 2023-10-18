package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.ApplicationState;

public interface StatementCreationResult {
    <T> T map(StatementCreationResultMapper<T> statementCreationResultMapper);

    class StatementCreated implements StatementCreationResult {
        private final Statement statement;

        public StatementCreated(Statement statement) {
            this.statement = statement;
        }

        @Override
        public <T> T map(StatementCreationResultMapper<T> statementCreationResultMapper) {
            return statementCreationResultMapper.mapStatementCreated(this.statement);
        }

        public Statement getStatement() {
            return statement;
        }
    }

    class NoSessionExists implements StatementCreationResult {
        @Override
        public <T> T map(StatementCreationResultMapper<T> statementCreationResultMapper) {
            return statementCreationResultMapper.mapNoSessionExists();
        }
    }

    class SessionInInvalidState implements StatementCreationResult {
        private final ApplicationState invalidState;

        public ApplicationState getInvalidState() {
            return invalidState;
        }

        public SessionInInvalidState(ApplicationState invalidState) {
            this.invalidState = invalidState;
        }

        @Override
        public <T> T map(StatementCreationResultMapper<T> statementCreationResultMapper) {
            return statementCreationResultMapper.mapSessionInInvalidState(this.invalidState);
        }
    }

}

