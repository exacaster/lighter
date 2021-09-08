package com.exacaster.lighter.backend.kubernetes;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;

public class KubernetesBackend implements Backend {
    private static final Logger LOG = getLogger(KubernetesBackend.class);

    private static final String SPARK_APP_TAG_LABEL = "spark-app-tag";
    private static final String SPARK_ROLE_LABEL = "spark-role";
    private static final String SPARK_APP_ID_LABEL = "spark-app-selector";

    private final KubernetesClient client;
    private final KubernetesProperties properties;
    private final AppConfiguration conf;

    public KubernetesBackend(KubernetesProperties properties, AppConfiguration conf) {
        this.properties = properties;
        this.conf = conf;
        this.client = new DefaultKubernetesClient();
    }

    @Override
    public Map<String, String> getSubmitConfiguration(Application application) {
        var props = new HashMap<>(Map.of(
                "spark.master", properties.getMaster(),
                "spark.kubernetes.driver.label." + SPARK_APP_TAG_LABEL, application.getId(),
                "spark.kubernetes.executor.label." + SPARK_APP_TAG_LABEL, application.getId(),
                "spark.kubernetes.submission.waitAppCompletion", "false",
                "spark.kubernetes.driverEnv.PY_GATEWAY_PORT", String.valueOf(conf.getPyGatewayPort())
        ));
        props.putAll(properties.getSubmitProps());
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
                        LOG.debug("Error Retrieving logs: {}", e.getStatus());
                    }
                    return Optional.ofNullable(log);
                })
                .map(log -> new Log(internalApplicationId, log));
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
