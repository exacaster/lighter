package com.exacaster.lighter.spark;

import static org.apache.spark.launcher.SparkLauncher.DRIVER_MEMORY;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_CORES;
import static org.apache.spark.launcher.SparkLauncher.EXECUTOR_MEMORY;

import java.io.IOException;
import java.util.Map;
import org.apache.spark.launcher.SparkLauncher;


public class SparkApp {

    private final SubmitParams submitParams;

    public SparkApp(SubmitParams submitParams) {
        this.submitParams = submitParams;
    }

    public void launch(Map<String, String> extraConfiguration) throws IOException {
        var launcher = new SparkLauncher()
                .setAppName(submitParams.name())
                .setDeployMode("cluster")
                .setAppResource(submitParams.file())
                .setMaster(submitParams.master());
        if (submitParams.mainClass() != null) {
            launcher.setMainClass(submitParams.mainClass());
        }
        submitParams.args().forEach(launcher::addAppArgs);
        submitParams.jars().forEach(launcher::addJar);
        submitParams.files().forEach(launcher::addFile);
        submitParams.pyFiles().forEach(launcher::addPyFile);
        submitParams.conf().forEach(launcher::setConf);
        extraConfiguration.forEach(launcher::setConf);
        launcher.setConf(DRIVER_MEMORY, submitParams.driverMemory())
                .setConf("spark.driver.cores", String.valueOf(submitParams.driverCores()))
                .setConf(EXECUTOR_CORES, String.valueOf(submitParams.executorCores()))
                .setConf(EXECUTOR_MEMORY, submitParams.executorMemory())
                .setConf("spark.executor.instances", String.valueOf(submitParams.numExecutors()));
        launcher.startApplication().disconnect();
    }

}
