package com.exacaster.lighter.backend.kubernetes;

import com.exacaster.lighter.backend.Backend;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import javax.inject.Singleton;

@Factory
@Requires(beans = KubernetesProperties.class)
public class KubernetesConfigurationFactory {

    @Singleton
    public Backend backend(KubernetesProperties properties) {
        return new KubernetesBackend(properties);
    }
}
