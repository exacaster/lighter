package com.exacaster.lighter.backend.kubernetes;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

@ConfigurationProperties("lighter.kubernetes")
@Requires(property="lighter.kubernetes.enabled", value = "true")
public record KubernetesProperties(String namespace, Integer maxLogSize) {

}
