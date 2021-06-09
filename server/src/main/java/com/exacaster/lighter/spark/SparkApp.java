package com.exacaster.lighter.spark;

import static org.apache.spark.launcher.SparkLauncher.DRIVER_MEMORY;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_CORES;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_MEMORY;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Map;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkAppHandle.Listener;
import org.apache.spark.launcher.SparkAppHandle.State;
import org.apache.spark.launcher.SparkLauncher;
import org.slf4j.Logger;


public class SparkApp {

    private static final Logger LOG = getLogger(SparkApp.class);

    private final SubmitParams submitParams;

    public SparkApp(SubmitParams submitParams) {
        this.submitParams = submitParams;
    }

    public void launch(Map<String, String> extraConfiguration) throws SubmitException {
        try {
            var launcher = new SparkLauncher()
                    .setAppName(submitParams.getName())
                    .setDeployMode("cluster")
                    .setAppResource(submitParams.getFile())
                    .setMaster(submitParams.getMaster());
            if (submitParams.getMainClass() != null) {
                launcher.setMainClass(submitParams.getMainClass());
            }
            submitParams.getArgs().forEach(launcher::addAppArgs);
            submitParams.getJars().forEach(launcher::addJar);
            submitParams.getFiles().forEach(launcher::addFile);
            submitParams.getPyFiles().forEach(launcher::addPyFile);
            submitParams.getConf().forEach(launcher::setConf);
            extraConfiguration.forEach(launcher::setConf);
            launcher.setConf(DRIVER_MEMORY, submitParams.getDriverMemory())
                    .setConf("spark.driver.cores", String.valueOf(submitParams.getDriverCores()))
                    .setConf(EXECUTOR_CORES, String.valueOf(submitParams.getExecutorCores()))
                    .setConf(EXECUTOR_MEMORY, submitParams.getExecutorMemory())
                    .setConf("spark.executor.instances", String.valueOf(submitParams.getNumExecutors()));
            launcher.startApplication()
                    .addListener(new Listener() {

                        @Override
                        public void stateChanged(SparkAppHandle handle) {
                            LOG.info("State change. AppId: {}, State: {}", handle.getAppId(), handle.getState());
                            LOG.info("Error: {}", handle.getError().map(Throwable::getMessage).orElse("not error"));
                            if (handle.getState().isFinal() || State.RUNNING.equals(handle.getState())) {
                                handle.disconnect();
                            }

                            // TODO: Fix error propagation
                            handle.getError().ifPresent(e -> {
                                throw new SubmitException(e);
                            });

                        }

                        @Override
                        public void infoChanged(SparkAppHandle handle) {
                            LOG.info("Error: {}", handle.getError().map(Throwable::getMessage).orElse("not error"));
                        }
                    });

        } catch (IOException | IllegalArgumentException e) {
            throw new SubmitException(e);
        }
    }

}
