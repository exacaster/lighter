package com.exacaster.lighter.backend.yarn;

import static java.util.stream.Stream.of;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.yarn.resources.State;
import com.exacaster.lighter.backend.yarn.resources.YarnApplication;
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationListResponse;
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationResponse;
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationWrapper;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class YarnBackend implements Backend {

    private final YarnClient client;
    private final AppConfiguration conf;

    public YarnBackend(YarnClient client, AppConfiguration conf) {
        this.client = client;
        this.conf = conf;
    }

    @Override
    public Optional<ApplicationInfo> getInfo(Application application) {
        return getYarnApplicationId(application)
                .map(id -> new ApplicationInfo(getState(id), id));
    }

    private ApplicationState getState(String id) {
        var yarnApplication = client.getApplication(id).getApp();
        switch (yarnApplication.getFinalStatus()) {
            case "NEW_SAVING":
            case "NEW":
                return ApplicationState.STARTING;
            case "SUBMITTED":
            case "ACCEPTED":
                return ApplicationState.IDLE;
            case "RUNNING":
                return ApplicationState.BUSY;
            case "FINISHED":
                return ApplicationState.SUCCESS;
            case "FAILED":
                return ApplicationState.ERROR;
            case "KILLED":
                return ApplicationState.KILLED;
            default:
                throw new IllegalStateException("Unexpected state: " + yarnApplication);
        }
    }

    @Override
    public Optional<Log> getLogs(Application application) {
        // TODO: extract yarn logs, returning tracking url for now
        return getYarnApplicationId(application).map(client::getApplication)
                .map(YarnApplicationResponse::getApp)
                .map(a -> new Log(application.getId(), a.getTrackingUrl()));
    }

    @Override
    public String getSessionJobResources() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("shell_wrapper.py").getFile());
        return file.getAbsolutePath();
    }

    @Override
    public void kill(Application application) {
        getYarnApplicationId(application)
                .ifPresent(id -> client.setState(id, new State("KILLED")));
    }

    @Override
    public Map<String, String> getSubmitConfiguration(Application application) {
        URI uri = URI.create(conf.getUrl());
        var host = uri.getHost();
        return Map.of(
                // TODO kerberos temp fix - move to catproxy if it works
                "spark.kerberos.keytab", System.getenv("KERBEROS_KEYTAB"),
                "spark.kerberos.principal", System.getenv("KERBEROS_PRINCIPAL"),
                "spark.master", "yarn",
                "spark.yarn.tags", "lighter," + application.getId(),
                "spark.yarn.submit.waitAppCompletion", "false",
                "spark.yarn.appMasterEnv.PY_GATEWAY_PORT", String.valueOf(conf.getPyGatewayPort()),
                "spark.yarn.appMasterEnv.PY_GATEWAY_HOST", host,
                "spark.yarn.appMasterEnv.LIGHTER_SESSION_ID", application.getId()
        );
    }

    private Optional<String> getYarnApplicationId(Application application) {
        return Optional.ofNullable(application.getAppId())
                .or(() -> of(client.getApps(application.getId()))
                        .map(YarnApplicationListResponse::getApps)
                        .filter(Objects::nonNull)
                        .map(YarnApplicationWrapper::getApp)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .findFirst()
                        .map(YarnApplication::getId));
    }
}
