package com.exacaster.lighter.backend.kubernetes;

import static com.exacaster.lighter.backend.CommonUtils.buildLauncherBase;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.SparkApp;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;

public class KubernetesBackend implements Backend {
    private static final Logger LOG = getLogger(KubernetesBackend.class);

    private static final String SPARK_APP_TAG_LABEL = "spark-app-tag";
    private static final String SPARK_ROLE_LABEL = "spark-role";
    private static final String SPARK_APP_ID_LABEL = "spark-app-selector";

    private static final Map<String, String> STATIC_SUBMIT_PROPS = Map.of(
            "spark.kubernetes.driver.podTemplateFile", "/home/app/k8s/driver_pod_template.yaml",
            "spark.kubernetes.executor.podTemplateFile", "/home/app/k8s/executor_pod_template.yaml",
            "spark.hadoop.fs.s3a.fast.upload", "true",
            "spark.kubernetes.submission.waitAppCompletion", "false"
    );

    private final KubernetesClient client;
    private final KubernetesProperties properties;
    private final AppConfiguration conf;

    public KubernetesBackend(KubernetesProperties properties, AppConfiguration conf, KubernetesClient client) {
        this.properties = properties;
        this.conf = conf;
        this.client = client;
    }

    @Override
    public SparkApp prepareSparkApplication(Application application, Map<String, String> configDefaults,
            Consumer<Throwable> errorHandler) {
        var conf = new HashMap<>(configDefaults);
        conf.putAll(getBackendConfiguration(application));

        var launcher = buildLauncherBase(application.getSubmitParams(), conf)
                .setDeployMode("cluster");
        return new SparkApp(launcher, errorHandler);
    }

    Map<String, String> getBackendConfiguration(Application application) {
        URI uri = URI.create(conf.getUrl());
        var host = uri.getHost();
        var props = new HashMap<String, String>();
        props.putAll(Map.of(
                "spark.kubernetes.namespace", properties.getNamespace(),
                "spark.kubernetes.authenticate.driver.serviceAccountName", properties.getServiceAccount(),
                "spark.master", properties.getMaster(),
                "spark.kubernetes.driver.label." + SPARK_APP_TAG_LABEL, application.getId(),
                "spark.kubernetes.executor.label." + SPARK_APP_TAG_LABEL, application.getId(),
                "spark.kubernetes.driverEnv.PY_GATEWAY_PORT", String.valueOf(conf.getPyGatewayPort()),
                "spark.kubernetes.driverEnv.PY_GATEWAY_HOST", host,
                "spark.kubernetes.driverEnv.LIGHTER_SESSION_ID", application.getId()
        ));
        props.putAll(STATIC_SUBMIT_PROPS);
        return props;
    }

    @Override
    public Optional<ApplicationInfo> getInfo(Application application) {
        var internalApplicationId = application.getId();
        return getDriverPod(internalApplicationId).map(pod -> new ApplicationInfo(mapStatus(pod.getStatus()),
                pod.getMetadata().getLabels().get(SPARK_APP_ID_LABEL)));
    }

    @Override
    public Optional<Log> getLogs(Application application) {
        var internalApplicationId = application.getId();
        return this.getDriverPod(internalApplicationId)
                .flatMap(pod -> {
                    String log = null;
                    try {
                        log = this.client.pods().inNamespace(properties.getNamespace())
                                .withName(pod.getMetadata().getName())
                                .tailingLines(properties.getMaxLogSize()).getLog(true);
                    } catch (KubernetesClientException e) {
                        LOG.warn("Error Retrieving logs: {}", e.getStatus());
                    }
                    return ofNullable(log);
                })
                .map(log -> new Log(internalApplicationId, log));
    }

    @Override
    public String getSessionJobResources() {
        return conf.getUrl() + "/lighter/jobs/shell_wrapper.py";
    }

    @Override
    public void kill(Application application) {
        var internalApplicationId = application.getId();
        this.getDriverPod(internalApplicationId)
                .ifPresent(pod -> this.client.pods()
                        .inNamespace(properties.getNamespace())
                        .withName(pod.getMetadata().getName()).delete());
    }

    private ApplicationState mapStatus(PodStatus status) {
        switch (status.getPhase()) {
            case "Unknown":
            case "Pending":
                return ApplicationState.STARTING;
            case "Running":
                return ApplicationState.BUSY;
            case "Succeeded":
                return ApplicationState.SUCCESS;
            case "Failed":
                return ApplicationState.DEAD;
            default:
                throw new IllegalStateException("Unexpected phase: " + status.getPhase());
        }
    }

    private Optional<Pod> getDriverPod(String appIdentifier) {
        return this.client.pods().inNamespace(properties.getNamespace())
                .withLabel(SPARK_APP_TAG_LABEL, appIdentifier)
                .withLabel(SPARK_ROLE_LABEL, "driver")
                .list()
                .getItems().stream().findFirst();
    }
}
