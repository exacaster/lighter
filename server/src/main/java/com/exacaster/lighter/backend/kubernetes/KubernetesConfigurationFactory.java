package com.exacaster.lighter.backend.kubernetes;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.configuration.AppConfiguration;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import javax.inject.Singleton;

@Factory
@Requires(beans = KubernetesProperties.class)
public class KubernetesConfigurationFactory {

    @Singleton
    public Backend backend(KubernetesProperties properties, AppConfiguration conf) {
        var client = new DefaultKubernetesClient();
        return new KubernetesBackend(properties, conf, client);
    }
}
