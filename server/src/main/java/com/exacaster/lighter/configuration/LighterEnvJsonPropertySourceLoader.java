package com.exacaster.lighter.configuration;

import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.jackson.core.env.EnvJsonPropertySourceLoader;

public class LighterEnvJsonPropertySourceLoader extends EnvJsonPropertySourceLoader {
    private final static String LIGHTER_APPLICATION_JSON = "LIGHTER_CONFIG_JSON";

    @Override
    protected String getEnvValue() {
        return CachedEnvironment.getenv(LIGHTER_APPLICATION_JSON);
    }
}
