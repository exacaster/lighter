package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.ApplicationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SessionUtils {

    private SessionUtils() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(SessionUtils.class);

    public static ApplicationState adjustState(boolean noWaitingStatements, ApplicationState state) {
        return switch (state) {
            case BUSY -> {
                if (noWaitingStatements) {
                    LOG.info("State BUSY -> IDLE: No waiting statements");
                    yield ApplicationState.IDLE;
                } else {
                    LOG.info("State remains BUSY: Has waiting statements");
                    yield state;
                }
            }
            case IDLE -> {
                if (noWaitingStatements) {
                    LOG.info("State remains IDLE: No waiting statements"); 
                    yield state;
                } else {
                    LOG.info("State IDLE -> BUSY: Has waiting statements");
                    yield ApplicationState.BUSY;
                }
            }
            default -> {
                LOG.info("State unchanged: {}", state);
                yield state;
            }
        };
    }
}
