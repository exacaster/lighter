package com.exacaster.lighter.backend.kubernetes;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.BatchInfo;
import com.exacaster.lighter.batch.Batch;
import com.exacaster.lighter.batch.BatchState;
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

    @Override
    public void configure(Map<String, String> configs) {
        this.client = new DefaultKubernetesClient();
        this.namespace = configs.getOrDefault("namespace", "default");
    }

    @Override
    public Map<String, String> getSubmitConfiguration(Batch batch) {
        return Map.of(
                "spark.kubernetes.driver.label." + SPARK_APP_TAG_LABEL, batch.appId(),
                "spark.kubernetes.executor.label." + SPARK_APP_TAG_LABEL, batch.appId(),
                );
    }

    @Override
    public Optional<BatchInfo> getInfo(String appIdentifier) {
        return getDriverPod(appIdentifier).map(pod -> new BatchInfo(mapStatus(pod.getStatus()),
                pod.getMetadata().getLabels().get(SPARK_APP_TAG_LABEL)));
    }

    private BatchState mapStatus(PodStatus status) {
        return switch (status.getPhase()) {
            case "Pending", "Unknown" -> BatchState.starting;
            case "Running" -> BatchState.busy;
            case "Succeeded" -> BatchState.success;
            case "Failed" -> BatchState.dead;
            default -> throw new IllegalStateException("Unexpected phase: " + status.getPhase());
        };
    }

    public Optional<String> getProessLogs(String appIdentifier) {
        return this.getDriverPod(appIdentifier)
                .map(pod -> this.client.pods().inNamespace(this.namespace)
                        .withName(pod.getMetadata().getName()).getLog(true));
    }

    private Optional<Pod> getDriverPod(String appIdentifier) {
        return this.client.pods().inNamespace(this.namespace)
                .withLabel(SPARK_APP_TAG_LABEL, appIdentifier)
                .withLabel(SPARK_ROLE_LABEL, "driver")
                .list()
                .getItems().stream().findFirst();
    }
}
