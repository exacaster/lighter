package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.ApplicationState;

public final class SessionUtils {

    private SessionUtils() {
    }

    public static ApplicationState adjustState(boolean noWaitingStatements, ApplicationState state) {
        return state.equals(ApplicationState.BUSY) && noWaitingStatements ? ApplicationState.IDLE : state;
    }
}
