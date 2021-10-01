package com.exacaster.lighter.backend.yarn;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.configuration.AppConfiguration;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import javax.inject.Singleton;

@Factory
@Requires(beans = YarnClient.class)
public class YarnConfigurationFactory {

    @Singleton
    public Backend backend(YarnProperties yarnProperties, YarnClient yarnClient, AppConfiguration conf) {
        return new YarnBackend(yarnProperties, yarnClient, conf);
    }
}
