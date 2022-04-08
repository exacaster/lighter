package com.exacaster.lighter.spark;

import static org.apache.spark.launcher.SparkLauncher.DRIVER_MEMORY;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_CORES;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_MEMORY;

import com.exacaster.lighter.concurrency.EmptyWaitable;
import com.exacaster.lighter.concurrency.Waitable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.spark.launcher.SparkLauncher;

public class SparkApp {
    private final SubmitParams submitParams;
    private final Consumer<Throwable> errorHandler;
    private final Collection<ConfigModifier> configModifiers;

    public SparkApp(SubmitParams submitParams, Consumer<Throwable> errorHandler,
            Collection<ConfigModifier> configModifiers) {
        this.submitParams = submitParams;
        this.errorHandler = errorHandler;
        this.configModifiers = new ArrayList<>();
        this.configModifiers.add(this::addSubmitParams);
        this.configModifiers.addAll(configModifiers);
    }

    private Map<String, String> addSubmitParams(Map<String, String> map) {
        var modified = new HashMap<>(map);
        if (!submitParams.getArchives().isEmpty()) {
            modified.put("spark.yarn.dist.archives", String.join(",", submitParams.getArchives()));
        }
        modified.putAll(submitParams.getConf());
        modified.put(DRIVER_MEMORY, submitParams.getDriverMemory());
        modified.put("spark.driver.cores", String.valueOf(submitParams.getDriverCores()));
        modified.put(EXECUTOR_CORES, String.valueOf(submitParams.getExecutorCores()));
        modified.put(EXECUTOR_MEMORY, submitParams.getExecutorMemory());
        modified.put("spark.executor.instances", String.valueOf(submitParams.getNumExecutors()));
        return modified;
    }

    public Waitable launch() {
        try {
            var launcher = prepareLauncher();
            if (submitParams.getMainClass() != null) {
                launcher.setMainClass(submitParams.getMainClass());
            }
            submitParams.getArgs().forEach(launcher::addAppArgs);
            submitParams.getJars().forEach(launcher::addJar);
            submitParams.getFiles().forEach(launcher::addFile);
            submitParams.getPyFiles().forEach(launcher::addPyFile);
            Map<String, String> configs = new HashMap<>();
            for (var modifier : this.configModifiers) {
                configs = modifier.apply(configs);
            }
            configs.forEach(launcher::setConf);
            var listener = new SparkListener(errorHandler);
            launcher.startApplication(listener);
            return listener;
        } catch (IOException | IllegalArgumentException e) {
            this.errorHandler.accept(e);
        }

        return EmptyWaitable.INSTANCE;
    }

    protected SparkLauncher prepareLauncher() {
        return new SparkLauncher()
                .setAppName(submitParams.getName())
                .setDeployMode("cluster")
                .setAppResource(submitParams.getFile());
    }

}
