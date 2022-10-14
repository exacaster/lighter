package com.exacaster.lighter.backend.local;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.SparkListener;
import com.exacaster.lighter.backend.local.logger.LogCollectingHandler;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkAppHandle.State;

public class LocalApp implements SparkListener {
    private static final org.slf4j.Logger LOG = getLogger(LocalApp.class);

    private final LogCollectingHandler logHandle;
    private final String loggerName;
    private final Consumer<Throwable> errorHandler;
    private SparkAppHandle handle;

    public LocalApp(Application application, Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        this.logHandle = new LogCollectingHandler(500);
        this.loggerName = "spark_app_" + application.getId();
        var logger = Logger.getLogger(loggerName);
        logger.addHandler(logHandle);
    }

    public Optional<ApplicationState> getState() {
        if (handle == null) {
            return Optional.empty();
        }

        switch (handle.getState()) {
            case UNKNOWN:
                return Optional.of(ApplicationState.NOT_STARTED);
            case CONNECTED:
            case SUBMITTED:
            case RUNNING:
                return Optional.of(ApplicationState.BUSY);
            case FINISHED:
                return Optional.of(ApplicationState.SUCCESS);
            case FAILED:
                return Optional.of(ApplicationState.ERROR);
            case LOST:
                return Optional.of(ApplicationState.DEAD);
            case KILLED:
                return Optional.of(ApplicationState.KILLED);
        }
        return Optional.empty();
    }

    public String getLog() {
        return logHandle.getLogs();
    }

    public void kill() {
        Objects.requireNonNull(handle);
        handle.kill();
        Logger.getLogger(loggerName).removeHandler(logHandle);
    }

    @Override
    public void onError(Throwable error) {
        errorHandler.accept(error);
    }

    @Override
    public void waitCompletion() throws InterruptedException {
        // Local application is not detached, until it is completed
        // do not block application processing for local applications.
    }

    @Override
    public void stateChanged(SparkAppHandle handle) {
        this.handle = handle;
        var state = handle.getState();
        LOG.info("State change. AppId: {}, State: {}", handle.getAppId(), state);
        handle.getError().ifPresent((error) -> {
            LOG.warn("State changed with error: {} ", error.getMessage());
            if (State.FAILED.equals(state)) {
                onError(error);
            }
        });
    }

    public String getLoggerName() {
        return loggerName;
    }

}
