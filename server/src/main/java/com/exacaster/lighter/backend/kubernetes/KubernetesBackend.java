package com.exacaster.lighter.backend.kubernetes;

import static com.exacaster.lighter.backend.Constants.DEPLOY_MODE_CLUSTER;
import static com.exacaster.lighter.backend.Constants.LIGHTER_SESSION_ID_ENV_NAME;
import static com.exacaster.lighter.backend.Constants.PY_GATEWAY_HOST_ENV_NAME;
import static com.exacaster.lighter.backend.Constants.PY_GATEWAY_PORT_ENV_NAME;
import static java.util.Optional.ofNullable;
import static org.apache.spark.launcher.SparkLauncher.DEPLOY_MODE;
import static org.apache.spark.launcher.SparkLauncher.SPARK_MASTER;
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

    private static final Map<String, String> OVERRIDABLE_STATIC_SUBMIT_PROPS = Map.of(
        "spark.hadoop.fs.s3a.fast.upload", "true",
        "spark.kubernetes.driver.podTemplateFile", "/home/app/k8s/driver_pod_template.yaml",
        "spark.kubernetes.executor.podTemplateFile", "/home/app/k8s/executor_pod_template.yaml"
    );
    private static final Map<String, String> STATIC_SUBMIT_PROPS = Map.of(
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
    public SparkApp prepareSparkApplication(
        Application application,
        Map<String, String> configDefaults,
        Consumer<Throwable> errorHandler
    ) {
        return new SparkApp(
            application,
            enrichConfigDefaults(configDefaults),
            getBackendConfiguration(application),
            errorHandler
        );
    }

    private Map<String, String> enrichConfigDefaults(Map<String, String> configDefaults) {
        var enrichedConfigs = new HashMap<>(OVERRIDABLE_STATIC_SUBMIT_PROPS);
        enrichedConfigs.putAll(configDefaults);
        return enrichedConfigs;
    }

    Map<String, String> getBackendConfiguration(Application application) {
        URI uri = URI.create(conf.getUrl());
        var host = uri.getHost();
        var props = new HashMap<String, String>();
        props.putAll(Map.of(
                DEPLOY_MODE, DEPLOY_MODE_CLUSTER,
                "spark.kubernetes.namespace", properties.getNamespace(),
                "spark.kubernetes.authenticate.driver.serviceAccountName", properties.getServiceAccount(),
                SPARK_MASTER, properties.getMaster(),
                "spark.kubernetes.driver.label." + SPARK_APP_TAG_LABEL, application.getId(),
                "spark.kubernetes.executor.label." + SPARK_APP_TAG_LABEL, application.getId(),
                "spark.kubernetes.driverEnv." + PY_GATEWAY_PORT_ENV_NAME, String.valueOf(conf.getPyGatewayPort()),
                "spark.kubernetes.driverEnv." + PY_GATEWAY_HOST_ENV_NAME, host,
                "spark.kubernetes.driverEnv." + LIGHTER_SESSION_ID_ENV_NAME, application.getId()
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
        return switch (status.getPhase()) {
            case "Unknown", "Pending" -> ApplicationState.STARTING;
            case "Running" -> ApplicationState.BUSY;
            case "Succeeded" -> ApplicationState.SUCCESS;
            case "Failed" -> ApplicationState.DEAD;
            default -> throw new IllegalStateException("Unexpected phase: " + status.getPhase());
        };
    }

    private Optional<Pod> getDriverPod(String appIdentifier) {
        return this.client.pods().inNamespace(properties.getNamespace())
                .withLabel(SPARK_APP_TAG_LABEL, appIdentifier)
                .withLabel(SPARK_ROLE_LABEL, "driver")
                .list()
                .getItems().stream().findFirst();
    }
}
