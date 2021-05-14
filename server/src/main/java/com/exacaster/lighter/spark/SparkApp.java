package com.exacaster.lighter.spark;

import javax.inject.Singleton;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkAppHandle.Listener;
import org.apache.spark.launcher.SparkLauncher;


public class SparkApp {

    private final SubmitParams submitParams;

    public SparkApp(SubmitParams submitParams) {
        this.submitParams = submitParams;
    }

    // TODO: fill missing configs
    public void launch() {
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
//        launcehr.startApplication(new Listener() {
//            @Override
//            public void stateChanged(SparkAppHandle handle) {
//
//            }
//
//            @Override
//            public void infoChanged(SparkAppHandle handle) {
//
//            }
//        })
    }

}
