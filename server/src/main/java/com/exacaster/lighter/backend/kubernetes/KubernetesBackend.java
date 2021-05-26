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

    private KubernetesClient client;
    private String namespace;
    private Integer maxLogSize;

    @Override
    public void configure(Map<String, String> configs) {
        this.client = new DefaultKubernetesClient();
        this.namespace = configs.getOrDefault("namespace", "default");
        this.maxLogSize = Integer.valueOf(configs.getOrDefault("logSize", "500"));
    }

    @Override
    public Map<String, String> getSubmitConfiguration(Application application) {
        return Map.of(
                "spark.kubernetes.driver.label." + SPARK_APP_TAG_LABEL, application.id(),
                "spark.kubernetes.executor.label." + SPARK_APP_TAG_LABEL, application.id()
        );
    }

    @Override
    public Optional<ApplicationInfo> getInfo(String internalApplicationId) {
        return getDriverPod(internalApplicationId).map(pod -> new ApplicationInfo(mapStatus(pod.getStatus()),
                pod.getMetadata().getLabels().get(SPARK_APP_ID_LABEL)));
    }

    private ApplicationState mapStatus(PodStatus status) {
        return switch (status.getPhase()) {
            case "Pending", "Unknown" -> ApplicationState.STARTING;
            case "Running" -> ApplicationState.BUSY;
            case "Succeeded" -> ApplicationState.SUCCESS;
            case "Failed" -> ApplicationState.DEAD;
            default -> throw new IllegalStateException("Unexpected phase: " + status.getPhase());
        };
    }

    @Override
    public Optional<Log> getLogs(String internalApplicationId) {
        return this.getDriverPod(internalApplicationId)
                .map(pod -> this.client.pods().inNamespace(this.namespace)
                        .withName(pod.getMetadata().getName())
                        .tailingLines(maxLogSize)
                        .getLog(true))
                .map(log -> new Log(internalApplicationId, log));
    }

    private Optional<Pod> getDriverPod(String appIdentifier) {
        return this.client.pods().inNamespace(this.namespace)
                .withLabel(SPARK_APP_TAG_LABEL, appIdentifier)
                .withLabel(SPARK_ROLE_LABEL, "driver")
                .list()
                .getItems().stream().findFirst();
    }
}
