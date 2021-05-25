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
        var launcehr = new SparkLauncher()
                .setAppName(submitParams.name())
                .setDeployMode("cluster")
                .setMaster(submitParams.master())
                .setMainClass(submitParams.mainClass());
        submitParams.args().forEach(launcehr::addAppArgs);
        submitParams.jars().forEach(launcehr::addJar);
        submitParams.files().forEach(launcehr::addFile);
        submitParams.pyFiles().forEach(launcehr::addPyFile);
        submitParams.conf().forEach(launcehr::setConf);
        extraConfiguration.forEach(launcehr::setConf);
        launcehr.setConf(DRIVER_MEMORY, submitParams.driverMemory())
                .setConf("spark.driver.cores", String.valueOf(submitParams.driverCores()))
                .setConf(EXECUTOR_CORES, String.valueOf(submitParams.executorCores()))
                .setConf(EXECUTOR_MEMORY, submitParams.executorMemory())
                .setConf("spark.executor.instances", String.valueOf(submitParams.numExecutors()));
        launcehr.startApplication().disconnect();
    }

}
