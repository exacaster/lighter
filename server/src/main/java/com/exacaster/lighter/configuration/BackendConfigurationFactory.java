package com.exacaster.lighter.configuration;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.backend.kubernetes.KubernetesBackend;
import io.micronaut.context.annotation.Factory;
import java.util.Map;
import javax.inject.Singleton;

@Factory
public class BackendConfigurationFactory {

    @Singleton
    public Backend backend() {
        var backend = new KubernetesBackend();
        // TODO
        backend.configure(Map.of("namespace", "spark"));
        return backend;
    }
}
