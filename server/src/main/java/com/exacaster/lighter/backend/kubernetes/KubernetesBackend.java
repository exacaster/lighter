package com.exacaster.lighter.backend.kubernetes;

import static java.util.Objects.requireNonNull;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.batch.Batch;
import com.exacaster.lighter.spark.SubmitParams;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Map;

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

    public void getProcessStatus(String appIdentifier) {
        var labeledPods = this.client.pods().inNamespace(this.namespace).withLabel(SPARK_APP_TAG_LABEL, appIdentifier)
                .list()
                .getItems();
        labeledPods.stream().findFirst().map(pod -> pod);
    }

    public void getProessLogs(String todo) {
        this.client.pods().inNamespace(this.namespace).withName("").getLog();
    }
}
