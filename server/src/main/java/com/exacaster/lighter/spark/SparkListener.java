package com.exacaster.lighter.spark;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.concurrency.Waitable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkAppHandle.Listener;
import org.slf4j.Logger;

public class SparkListener implements Listener, Waitable {

    private static final Logger LOG = getLogger(SparkListener.class);
    private final Consumer<Throwable> errorHandler;
    private final CountDownLatch latch;

    public SparkListener(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void stateChanged(SparkAppHandle handle) {
        LOG.info("State change. AppId: {}, State: {}", handle.getAppId(), handle.getState());
        handle.getError().ifPresent(errorHandler);
        if (handle.getState() != null && handle.getState().isFinal()) {
            handle.disconnect();
            latch.countDown();
        }
    }

    @Override
    public void infoChanged(SparkAppHandle handle) {
        // TODO: ?
    }

    @Override
    public void waitCompletion() throws InterruptedException {
        latch.await();
    }
}
