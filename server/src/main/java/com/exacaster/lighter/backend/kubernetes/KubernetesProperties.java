package com.exacaster.lighter.backend.kubernetes;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import java.util.StringJoiner;

@ConfigurationProperties("lighter.kubernetes")
@Requires(property="lighter.kubernetes.enabled", value = "true")
public class KubernetesProperties{

    private final String namespace;
    private final Integer maxLogSize;
    private final String master;
    private final String serviceAccount;

    @ConfigurationInject
    public KubernetesProperties(String namespace, Integer maxLogSize, String master, String serviceAccount) {
        this.namespace = namespace;
        this.maxLogSize = maxLogSize;
        this.master = master;
        this.serviceAccount = serviceAccount;
    }

    public String getNamespace() {
        return namespace;
    }

    public Integer getMaxLogSize() {
        return maxLogSize;
    }

    public String getMaster() {
        return master;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", KubernetesProperties.class.getSimpleName() + "[", "]")
                .add("namespace='" + namespace + "'")
                .add("maxLogSize=" + maxLogSize)
                .add("master=" + master)
                .add("serviceAccount=" + serviceAccount)
                .toString();
    }
}
