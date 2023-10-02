package com.exacaster.lighter.backend.local;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.SparkApp;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.exacaster.lighter.backend.Constants.DEPLOY_MODE_CLIENT;
import static com.exacaster.lighter.backend.Constants.LIGHTER_CONF_PREFIX;
import static com.exacaster.lighter.backend.Constants.LIGHTER_SESSION_ID_ENV_NAME;
import static com.exacaster.lighter.backend.Constants.PY_GATEWAY_HOST_ENV_NAME;
import static com.exacaster.lighter.backend.Constants.PY_GATEWAY_PORT_ENV_NAME;
import static org.apache.spark.launcher.SparkLauncher.CHILD_PROCESS_LOGGER_NAME;
import static org.apache.spark.launcher.SparkLauncher.DEPLOY_MODE;
import static org.apache.spark.launcher.SparkLauncher.SPARK_MASTER;

public class LocalBackend implements Backend {

    private final static String LOCAL_ENV_CONF_PREFIX = LIGHTER_CONF_PREFIX + "local.env.";

    private final AppConfiguration conf;

    private final Cache<String, LocalApp> activeApps = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .<String, LocalApp>removalListener(it -> it.getValue().kill())
            .build();

    public LocalBackend(AppConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public Optional<ApplicationInfo> getInfo(Application application) {
        return handleForApp(application)
                .flatMap(LocalApp::getState)
                .map(it -> new ApplicationInfo(it, application.getId()));
    }

    @Override
    public Optional<Log> getLogs(Application application) {
        return handleForApp(application)
                .map(LocalApp::getLog)
                .map(it -> new Log(application.getId(), it));
    }

    @Override
    public String getSessionJobResources() {
        return conf.getUrl() + "/lighter/jobs/shell_wrapper.py";
    }

    @Override
    public void kill(Application application) {
        handleForApp(application).ifPresent(LocalApp::kill);
        activeApps.invalidate(application.getId());
    }

    @Override
    public SparkApp prepareSparkApplication(Application application,
            Map<String, String> configDefaults,
            Consumer<Throwable> errorHandler) {
        var localApp = new LocalApp(application, errorHandler);
        activeApps.put(application.getId(), localApp);

        return new SparkApp(
                application,
                configDefaults,
                Map.of(
                        DEPLOY_MODE, DEPLOY_MODE_CLIENT,
                        SPARK_MASTER, "local[*]",
                        CHILD_PROCESS_LOGGER_NAME, localApp.getLoggerName()
                ),
                buildEnvironment(application),
                localApp
        );
    }

    private Map<String, String> buildEnvironment(Application application) {
        var env = new HashMap<String, String>();
        application.getSubmitParams()
                .getConf()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(LOCAL_ENV_CONF_PREFIX))
                .forEach(entry -> {
                    var key = entry.getKey().replaceFirst(LOCAL_ENV_CONF_PREFIX, "");
                    env.put(key, entry.getValue());
                });
        env.putAll(Map.of(
                LIGHTER_SESSION_ID_ENV_NAME, application.getId(),
                PY_GATEWAY_PORT_ENV_NAME, conf.getPyGatewayPort().toString(),
                PY_GATEWAY_HOST_ENV_NAME, "localhost"
        ));
        return env;
    }

    Optional<LocalApp> handleForApp(Application application) {
        return Optional.ofNullable(activeApps.getIfPresent(application.getId()));
    }
}
