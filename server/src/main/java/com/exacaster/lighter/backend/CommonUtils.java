package com.exacaster.lighter.backend;

import static org.apache.spark.launcher.SparkLauncher.DRIVER_MEMORY;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_CORES;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_MEMORY;

import com.exacaster.lighter.application.SubmitParams;
import java.util.Map;
import org.apache.spark.launcher.SparkLauncher;

public final class CommonUtils {

    private CommonUtils() {
    }

    public static SparkLauncher buildLauncherBase(SubmitParams submitParams, Map<String, String> configs) {
        var launcher = new SparkLauncher()
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

        configs.forEach(launcher::setConf);
        submitParams.getConf().forEach(launcher::setConf);
        return launcher;
    }
}
