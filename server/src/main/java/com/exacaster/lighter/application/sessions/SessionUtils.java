package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.ApplicationState;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public final class SessionUtils {
    private static final Collection<ApplicationState> ADJUSTABLE_STATES = List.of(
            ApplicationState.BUSY,
            ApplicationState.IDLE
    );
    private static final Logger LOG = getLogger(SessionUtils.class);

    private SessionUtils() {
    }

    public static ApplicationState adjustState(boolean hasWaitingStatements, ApplicationState state) {
        LOG.debug("Adjusting state - has waiting statements {}, app state: {} ", hasWaitingStatements, state);
        if (ADJUSTABLE_STATES.contains(state)) {
            return hasWaitingStatements ? ApplicationState.BUSY : ApplicationState.IDLE;
        }

        return state;
    }
}
