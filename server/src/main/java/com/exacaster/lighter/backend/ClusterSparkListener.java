package com.exacaster.lighter.backend;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkAppHandle.State;
import org.slf4j.Logger;

public class ClusterSparkListener implements SparkListener {

    private static final Logger LOG = getLogger(ClusterSparkListener.class);
    private final Consumer<Throwable> errorHandler;
    private final CountDownLatch latch;

    public ClusterSparkListener(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void stateChanged(SparkAppHandle handle) {
        var state = handle.getState();
        LOG.info("State change. AppId: {}, State: {}", handle.getAppId(), state);
        handle.getError().ifPresent((error) -> {
            LOG.warn("State changed with error: {} ", error.getMessage());
            if (State.FAILED.equals(state)) {
                onError(error);
            }
        });
        // Disconnect when final or submitted.
        // In case app fails after detach, status will be retrieved by ApplicationStatusHandler.
        if (state != null && (state.isFinal() || State.SUBMITTED.equals(state))) {
            handle.disconnect();
            latch.countDown();
        }
    }

    @Override
    public void waitCompletion() throws InterruptedException {
        latch.await();
    }

    @Override
    public void onError(Throwable error) {
        errorHandler.accept(error);
    }
}
