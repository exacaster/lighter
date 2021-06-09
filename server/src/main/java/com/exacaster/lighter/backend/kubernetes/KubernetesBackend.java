package com.exacaster.lighter.backend.kubernetes;

import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.log.Log;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Map;
import java.util.Optional;

public class KubernetesBackend implements Backend {

    private static final String SPARK_APP_TAG_LABEL = "spark-app-tag";
    private static final String SPARK_ROLE_LABEL = "spark-role";
    private static final String SPARK_APP_ID_LABEL = "spark-app-selector";

    private final KubernetesClient client;
    private final KubernetesProperties properties;

    public KubernetesBackend(KubernetesProperties properties) {
        this.properties = properties;
        this.client = new DefaultKubernetesClient();
    }

    @Override
    public Map<String, String> getSubmitConfiguration(Application application) {
        return Map.of(
                "spark.kubernetes.driver.label." + SPARK_APP_TAG_LABEL, application.getId(),
                "spark.kubernetes.executor.label." + SPARK_APP_TAG_LABEL, application.getId()
        );
    }

    @Override
    public Optional<ApplicationInfo> getInfo(String internalApplicationId) {
        return getDriverPod(internalApplicationId).map(pod -> new ApplicationInfo(mapStatus(pod.getStatus()),
                pod.getMetadata().getLabels().get(SPARK_APP_ID_LABEL)));
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

    @Override
    public Optional<Log> getLogs(String internalApplicationId) {
        return this.getDriverPod(internalApplicationId)
                .map(pod -> this.client.pods().inNamespace(properties.getNamespace())
                        .withName(pod.getMetadata().getName())
                        .tailingLines(properties.getMaxLogSize())
                        .getLog(true))
                .map(log -> new Log(internalApplicationId, log));
    }

    private Optional<Pod> getDriverPod(String appIdentifier) {
        return this.client.pods().inNamespace(properties.getNamespace())
                .withLabel(SPARK_APP_TAG_LABEL, appIdentifier)
                .withLabel(SPARK_ROLE_LABEL, "driver")
                .list()
                .getItems().stream().findFirst();
    }
}
