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

    @ConfigurationInject
    public KubernetesProperties(String namespace, Integer maxLogSize) {
        this.namespace = namespace;
        this.maxLogSize = maxLogSize;
    }

    public String getNamespace() {
        return namespace;
    }

    public Integer getMaxLogSize() {
        return maxLogSize;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", KubernetesProperties.class.getSimpleName() + "[", "]")
                .add("namespace='" + namespace + "'")
                .add("maxLogSize=" + maxLogSize)
                .toString();
    }
}
