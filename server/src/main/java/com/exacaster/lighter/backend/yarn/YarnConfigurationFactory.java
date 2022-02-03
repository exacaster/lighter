package com.exacaster.lighter.backend.yarn;

import static org.apache.hadoop.yarn.conf.YarnConfiguration.RM_ADDRESS;
import static org.apache.hadoop.yarn.conf.YarnConfiguration.RM_KEYTAB;
import static org.apache.hadoop.yarn.conf.YarnConfiguration.RM_PRINCIPAL;

import com.exacaster.lighter.configuration.AppConfiguration;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

@Factory
@Requires(beans = YarnProperties.class)
public class YarnConfigurationFactory {

    @Singleton
    public YarnBackend backend(YarnProperties yarnProperties, AppConfiguration conf) {
        YarnConfiguration yarnConfiguration = new YarnConfiguration();
        if (yarnProperties.getAddress() != null) {
            yarnConfiguration.set(RM_ADDRESS, yarnProperties.getAddress());
        }
        if (yarnProperties.getKerberosKeytab() != null && yarnProperties.getKerberosPrincipal() != null) {
            yarnConfiguration.set(RM_PRINCIPAL, yarnProperties.getKerberosPrincipal());
            yarnConfiguration.set(RM_KEYTAB, yarnProperties.getKerberosKeytab());
        }
        var yarnClient = YarnClient.createYarnClient();
        yarnClient.init(yarnConfiguration);
        yarnClient.start();
        return new YarnBackend(yarnProperties, yarnClient, conf);
    }
}
