package com.exacaster.lighter.backend.yarn;

import static com.exacaster.lighter.backend.CommonUtils.buildLauncherBase;
import static org.apache.hadoop.yarn.api.records.ApplicationId.fromString;
import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.SparkApp;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsRequest;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;

public class YarnBackend implements Backend {

    private static final Logger LOG = getLogger(YarnBackend.class);

    private final YarnProperties yarnProperties;
    private final YarnClient client;
    private final AppConfiguration conf;

    public YarnBackend(YarnProperties yarnProperties, YarnClient client, AppConfiguration conf) {
        this.yarnProperties = yarnProperties;
        this.client = client;
        this.conf = conf;
    }

    @Override
    public Optional<ApplicationInfo> getInfo(Application application) {
        return getYarnApplicationId(application)
                .map(id -> new ApplicationInfo(getState(id), id));
    }

    private ApplicationState getState(String id) {
        try {
            var yarnApplication = client.getApplicationReport(fromString(id));
            switch (yarnApplication.getFinalApplicationStatus()) {
                case UNDEFINED:
                    return ApplicationState.BUSY;
                case SUCCEEDED:
                    return ApplicationState.SUCCESS;
                case FAILED:
                    return ApplicationState.ERROR;
                case KILLED:
                    return ApplicationState.KILLED;
            }
        } catch (YarnException | IOException e) {
            LOG.error("Unexpected error for appId: {}", id, e);
        }
        throw new IllegalStateException("Unexpected state for appId: " + id);
    }

    @Override
    public Optional<Log> getLogs(Application application) {
        // TODO: extract yarn logs, returning tracking url for now
        return getYarnApplicationId(application)
                .map(ApplicationId::fromString)
                .map(appId -> {
                    try {
                        return client.getApplicationReport(appId);
                    } catch (YarnException | IOException e) {
                        LOG.warn("Failed to get logs for app: {}", application, e);
                        return null;
                    }
                })
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
                .ifPresent(id -> {
                    try {
                        client.killApplication(ApplicationId.fromString(id));
                    } catch (YarnException | IOException e) {
                        LOG.error("Can't kill Yarn app: {}", application, e);
                        throw new IllegalStateException(e);
                    }
                });
    }

    @Override
    public SparkApp prepareSparkApplication(Application application, Map<String, String> configDefaults,
            Consumer<Throwable> errorHandler) {
        var conf = new HashMap<>(configDefaults);
        conf.putAll(getSubmitConfiguration(application));

        var launcher = buildLauncherBase(application.getSubmitParams(), conf)
                .setDeployMode("cluster");
        return new SparkApp(launcher, errorHandler);
    }

    Map<String, String> getSubmitConfiguration(Application application) {
        URI uri = URI.create(conf.getUrl());
        var host = uri.getHost();
        var props = new HashMap<String, String>();
        props.putAll(Map.of(
                "spark.master", "yarn",
                "spark.yarn.tags", "lighter," + application.getId(),
                "spark.yarn.submit.waitAppCompletion", "false",
                "spark.yarn.appMasterEnv.PY_GATEWAY_PORT", String.valueOf(conf.getPyGatewayPort()),
                "spark.yarn.appMasterEnv.PY_GATEWAY_HOST", host,
                "spark.yarn.appMasterEnv.LIGHTER_SESSION_ID", application.getId()
        ));
        if (!props.containsKey("spark.kerberos.keytab") && yarnProperties.getKerberos() != null) {
            props.put("spark.kerberos.keytab", yarnProperties.getKerberos().getKeytab());
            props.put("spark.kerberos.principal", yarnProperties.getKerberos().getPrincipal());
        }
        return props;
    }

    private Optional<String> getYarnApplicationId(Application application) {
        return Optional.ofNullable(application.getAppId())
                .or(() -> {
                    try {
                        var request = GetApplicationsRequest.newInstance();
                        request.setApplicationTags(Set.of(application.getId()));
                        return client.getApplications(request).stream()
                                .max(Comparator.comparing(ApplicationReport::getStartTime))
                                .map(ApplicationReport::getApplicationId)
                                .map(ApplicationId::toString);
                    } catch (YarnException | IOException e) {
                        LOG.error("Failed to get app id for app: {}", application, e);
                        throw new IllegalStateException(e);
                    }
                });
    }
}
