package com.exacaster.lighter.backend.local;

import static org.apache.spark.launcher.SparkLauncher.CHILD_PROCESS_LOGGER_NAME;
import static org.apache.spark.launcher.SparkLauncher.DEPLOY_MODE;
import static org.apache.spark.launcher.SparkLauncher.SPARK_MASTER;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.SparkApp;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class LocalBackend implements Backend {

    private final AppConfiguration conf;
    private final Map<String, LocalApp> activeApps = new HashMap<>();

    public LocalBackend(AppConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public Optional<ApplicationInfo> getInfo(Application application) {
        var localApp = activeApps.get(application.getId());
        return Optional.ofNullable(localApp)
                .flatMap(LocalApp::getState)
                .map(it -> new ApplicationInfo(it, application.getId()));
    }

    @Override
    public Optional<Log> getLogs(Application application) {
        var localApp = activeApps.get(application.getId());
        return Optional.ofNullable(localApp)
                .map(LocalApp::getLog)
                .map(it -> new Log(application.getId(), it));
    }

    @Override
    public String getSessionJobResources() {
        return conf.getUrl() + "/lighter/jobs/shell_wrapper.py";
    }

    @Override
    public void kill(Application application) {
        var handle = activeApps.remove(application.getId());
        if (handle != null) handle.kill();
    }

    @Override
    public SparkApp prepareSparkApplication(Application application, Map<String, String> configDefaults,
            Consumer<Throwable> errorHandler) {
        var localApp = new LocalApp(application, errorHandler, () -> activeApps.remove(application.getId()));
        activeApps.put(application.getId(), localApp);
        return new SparkApp(
                application,
                configDefaults,
                Map.of(
                        DEPLOY_MODE, "client",
                        SPARK_MASTER, "local",
                        CHILD_PROCESS_LOGGER_NAME, localApp.getLoggerName()
                ),
                localApp
        );
    }
}
