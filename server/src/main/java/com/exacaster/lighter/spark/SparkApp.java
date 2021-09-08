package com.exacaster.lighter.spark;

import static org.apache.spark.launcher.SparkLauncher.DRIVER_MEMORY;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_CORES;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_MEMORY;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkAppHandle.Listener;
import org.apache.spark.launcher.SparkLauncher;
import org.slf4j.Logger;

public class SparkApp {

    private static final Logger LOG = getLogger(SparkApp.class);

    private final SubmitParams submitParams;
    private final Consumer<Throwable> errorHandler;

    public SparkApp(SubmitParams submitParams, Consumer<Throwable> errorHandler) {
        this.submitParams = submitParams;
        this.errorHandler = errorHandler;
    }

    public void launch(Map<String, String> extraConfiguration) {
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
            extraConfiguration.forEach(launcher::setConf);
            submitParams.getConf().forEach(launcher::setConf);
            launcher.setConf(DRIVER_MEMORY, submitParams.getDriverMemory())
                    .setConf("spark.driver.cores", String.valueOf(submitParams.getDriverCores()))
                    .setConf(EXECUTOR_CORES, String.valueOf(submitParams.getExecutorCores()))
                    .setConf(EXECUTOR_MEMORY, submitParams.getExecutorMemory())
                    .setConf("spark.executor.instances", String.valueOf(submitParams.getNumExecutors()));
            launcher.startApplication(new Listener() {
                        @Override
                        public void stateChanged(SparkAppHandle handle) {
                            LOG.info("State change. AppId: {}, State: {}", handle.getAppId(), handle.getState());
                            handle.getError().ifPresent(errorHandler);
                        }

                        @Override
                        public void infoChanged(SparkAppHandle handle) {
                            // TODO: ?
                        }
                    });

        } catch (IOException | IllegalArgumentException e) {
            this.errorHandler.accept(e);
        }
    }

}
