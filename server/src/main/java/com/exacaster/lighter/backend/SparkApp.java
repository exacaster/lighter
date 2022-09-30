package com.exacaster.lighter.backend;

import com.exacaster.lighter.concurrency.EmptyWaitable;
import com.exacaster.lighter.concurrency.Waitable;
import java.io.IOException;
import java.util.function.Consumer;
import org.apache.spark.launcher.SparkLauncher;

public class SparkApp {
    private final Consumer<Throwable> errorHandler;
    private final SparkLauncher launcher;

    public SparkApp(SparkLauncher launcher, Consumer<Throwable> errorHandler) {
        this.launcher = launcher;
        this.errorHandler = errorHandler;
    }

    public Waitable launch() {
        try {
            var listener = new SparkListener(errorHandler);
            launcher.startApplication(listener);
            return listener;
        } catch (IOException | IllegalArgumentException e) {
            this.errorHandler.accept(e);
        }

        return EmptyWaitable.INSTANCE;
    }

}
