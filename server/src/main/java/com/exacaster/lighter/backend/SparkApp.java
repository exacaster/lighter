package com.exacaster.lighter.backend;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.concurrency.EmptyWaitable;
import com.exacaster.lighter.concurrency.Waitable;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.apache.spark.launcher.SparkLauncher.DRIVER_MEMORY;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_CORES;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_MEMORY;

public class SparkApp {

    private final Map<String, String> configDefaults;
    private final Map<String, String> backendConfiguration;
    private final Map<String, String> envVariables;
    private final Application application;
    private final SparkListener listener;

    public SparkApp(Application application,
            Map<String, String> configDefaults,
            Map<String, String> backendConfiguration,
            Consumer<Throwable> errorHandler) {
        this(application, configDefaults, backendConfiguration, Collections.emptyMap(), new ClusterSparkListener(errorHandler));
    }

    public SparkApp(Application application,
                    Map<String, String> configDefaults,
                    Map<String, String> backendConfiguration,
                    Map<String, String> envVariables,
                    SparkListener listener) {
        this.application = application;
        this.configDefaults = configDefaults;
        this.backendConfiguration = backendConfiguration;
        this.envVariables = envVariables;
        this.listener = listener;
    }

    public Waitable launch() {
        try {
            var launcher = buildLauncher();
            launcher.startApplication(listener);
            return listener;
        } catch (IOException | IllegalArgumentException e) {
            this.listener.onError(e);
        }

        return EmptyWaitable.INSTANCE;
    }

    private SparkLauncher buildLauncher() {
        var submitParams = application.getSubmitParams();
        var launcher = new SparkLauncher(envVariables)
                .setAppName(submitParams.getName())
                .setAppResource(submitParams.getFile());

        if (!submitParams.getArchives().isEmpty()) {
            launcher.setConf("spark.yarn.dist.archives", String.join(",", submitParams.getArchives()));
        }
        launcher.setConf(DRIVER_MEMORY, submitParams.getDriverMemory());
        launcher.setConf("spark.driver.cores", String.valueOf(submitParams.getDriverCores()));
        launcher.setConf(EXECUTOR_CORES, String.valueOf(submitParams.getExecutorCores()));
        launcher.setConf(EXECUTOR_MEMORY, submitParams.getExecutorMemory());
        launcher.setConf("spark.executor.instances", String.valueOf(submitParams.getNumExecutors()));

        if (submitParams.getMainClass() != null) {
            launcher.setMainClass(submitParams.getMainClass());
        }
        submitParams.getArgs().forEach(launcher::addAppArgs);
        submitParams.getJars().forEach(launcher::addJar);
        submitParams.getFiles().forEach(launcher::addFile);
        submitParams.getPyFiles().forEach(launcher::addPyFile);

        configDefaults.forEach(launcher::setConf);
        submitParams.getConf().forEach(launcher::setConf);
        backendConfiguration.forEach(launcher::setConf);

        return launcher;
    }

}
