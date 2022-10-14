package com.exacaster.lighter.backend.kubernetes;

import com.exacaster.lighter.configuration.AppConfiguration;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Factory
@Requires(beans = KubernetesProperties.class)
public class KubernetesConfigurationFactory {

    @Singleton
    public KubernetesBackend backend(KubernetesProperties properties, AppConfiguration conf) {
        var client = new KubernetesClientBuilder().build();
        return new KubernetesBackend(properties, conf, client);
    }
}
