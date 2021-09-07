package com.exacaster.lighter.backend.yarn;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.yarn.resources.State;
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationResponse;
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationWrapper;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        var yarnState = client.getState(id);
        switch (yarnState.getState()) {
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
                throw new IllegalStateException("Unexpected state: " + yarnState);
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
    public void kill(Application application) {
        getYarnApplicationId(application)
                .ifPresent(id -> client.setState(id, new State("KILLED")));
    }

    @Override
    public Map<String, String> getSubmitConfiguration(Application application) {
        return Map.of(
                "spark.yarn.tags", "lighter," + application.getAppId(),
                "spark.yarn.submit.waitAppCompletion", "false",
                "spark.yarn.appMasterEnv.PY_GATEWAY_PORT", String.valueOf(conf.getPyGatewayPort())
        );
    }

    private Optional<String> getYarnApplicationId(Application application) {
        return Optional.ofNullable(application.getAppId())
                .or(() -> {
                    var yarnApps = client.getApps("lighter," + application.getAppId())
                            .getApps().stream()
                            .map(YarnApplicationWrapper::getApp)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    if (yarnApps.isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(yarnApps.get(0).getId());
                });
    }
}
