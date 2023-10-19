package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.ApplicationState;

public interface StatementCreationResultMapper<T> {
    T mapStatementCreated(Statement sessionCreated);
    T mapNoSessionExists();
    T mapSessionInInvalidState(ApplicationState sessionState);
}

