package com.exacaster.lighter.spark;

import static org.apache.spark.launcher.SparkLauncher.DRIVER_MEMORY;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_CORES;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_MEMORY;

import com.exacaster.lighter.concurrency.EmptyWaitable;
import com.exacaster.lighter.concurrency.Waitable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.spark.launcher.SparkLauncher;

public class SparkApp {
    private final SubmitParams submitParams;
    private final Consumer<Throwable> errorHandler;

    public SparkApp(SubmitParams submitParams, Consumer<Throwable> errorHandler) {
        this.submitParams = submitParams;
        this.errorHandler = errorHandler;
    }

    public Waitable launch(Map<String, String> extraConfiguration) {
        try {
            var launcher = new SparkLauncher()
                    .setAppName(submitParams.getName())
                    .setDeployMode("cluster")
                    .setAppResource(submitParams.getFile());
            if (submitParams.getMainClass() != null) {
                launcher.setMainClass(submitParams.getMainClass());
            }
            submitParams.getArgs().forEach(launcher::addAppArgs);
            submitParams.getJars().forEach(launcher::addJar);
            submitParams.getFiles().forEach(launcher::addFile);
            submitParams.getPyFiles().forEach(launcher::addPyFile);
            if (!submitParams.getArchives().isEmpty()) {
                launcher.setConf("spark.yarn.dist.archives", String.join(",", submitParams.getArchives()));
            }
            extraConfiguration.forEach(launcher::setConf);
            submitParams.getConf().forEach(launcher::setConf);
            launcher.setConf(DRIVER_MEMORY, submitParams.getDriverMemory())
                    .setConf("spark.driver.cores", String.valueOf(submitParams.getDriverCores()))
                    .setConf(EXECUTOR_CORES, String.valueOf(submitParams.getExecutorCores()))
                    .setConf(EXECUTOR_MEMORY, submitParams.getExecutorMemory())
                    .setConf("spark.executor.instances", String.valueOf(submitParams.getNumExecutors()));
            var listener = new SparkListener(errorHandler);
            launcher.startApplication(listener);
            return listener;
        } catch (IOException | IllegalArgumentException e) {
            this.errorHandler.accept(e);
        }

        return EmptyWaitable.INSTANCE;
    }

}
