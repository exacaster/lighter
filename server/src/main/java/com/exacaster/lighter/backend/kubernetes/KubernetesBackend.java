package com.exacaster.lighter.backend.kubernetes;

import com.exacaster.lighter.backend.Backend;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;

public class KubernetesBackend implements Backend {

    private final CoreV1Api api;

    public KubernetesBackend() {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        this.api = new CoreV1Api();
    }

    public void getProcess() {
        this.api.connectGetNamespacedPodAttach();
        PodLogs logs = new PodLogs();
        V1Pod pod = api.listNamespacedPod("default", )
    }
}
