package com.exacaster.lighter.backend.kubernetes;

import static java.util.Objects.requireNonNull;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.BatchInfo;
import com.exacaster.lighter.batch.Batch;
import com.exacaster.lighter.batch.BatchState;
import com.exacaster.lighter.spark.SubmitParams;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Map;
import java.util.Optional;

public class KubernetesBackend implements Backend {

    private final static String SPARK_APP_TAG_LABEL = "spark-app-tag";

    private KubernetesClient client;
    private String namespace;

    public KubernetesBackend() {

    }

    @Override
    public void configure(Map<String, String> configs) {
        this.client = new DefaultKubernetesClient();
        this.namespace = requireNonNull(configs.get("namespace"));
    }

    @Override
    public SubmitParams getSubmitParamas(Batch batch) {
        return null;
    }

    @Override
    public Optional<BatchInfo> getInfo(String appIdentifier) {
        var labeledPods = this.client.pods().inNamespace(this.namespace).withLabel(SPARK_APP_TAG_LABEL, appIdentifier)
                .list()
                .getItems();
        return labeledPods.stream().findFirst().map(pod -> new BatchInfo(mapStatus(pod.getStatus())));
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

    public void getProessLogs(String todo) {
        this.client.pods().inNamespace(this.namespace).withName("").getLog();
    }
}
