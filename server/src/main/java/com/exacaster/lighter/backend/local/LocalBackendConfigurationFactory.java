package com.exacaster.lighter.backend.local;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.configuration.AppConfiguration;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Factory
public class LocalBackendConfigurationFactory {

    @Singleton
    @Requires(missingBeans = Backend.class)
    public LocalBackend backend(AppConfiguration conf) {
        return new LocalBackend(conf);
    }
}
