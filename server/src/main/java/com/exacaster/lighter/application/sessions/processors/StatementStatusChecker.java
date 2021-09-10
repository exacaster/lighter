package com.exacaster.lighter.application.sessions.processors;

import com.exacaster.lighter.application.Application;

public interface StatementStatusChecker {
    boolean hasWaitingStatement(Application application);
}
