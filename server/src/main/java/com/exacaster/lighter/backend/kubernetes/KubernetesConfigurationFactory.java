package com.exacaster.lighter.backend.kubernetes;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.kubernetes.KubernetesBackend;
import com.exacaster.lighter.backend.kubernetes.KubernetesProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import java.util.Map;
import javax.inject.Singleton;

@Factory
@Requires(beans = {KubernetesProperties.class})
public class KubernetesConfigurationFactory {

    @Singleton
    public Backend backend(KubernetesProperties properties) {
        return new KubernetesBackend(properties);
    }
}
