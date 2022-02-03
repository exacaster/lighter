package com.exacaster.lighter.backend.yarn;

import static org.apache.hadoop.yarn.conf.YarnConfiguration.RM_KEYTAB;
import static org.apache.hadoop.yarn.conf.YarnConfiguration.RM_PRINCIPAL;

import com.exacaster.lighter.configuration.AppConfiguration;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.client.api.YarnClient;

@Factory
@Requires(beans = YarnProperties.class)
public class YarnConfigurationFactory {

    @Singleton
    public YarnBackend backend(YarnProperties yarnProperties, AppConfiguration conf,
            @Property(name = "hadoop.conf.dir") String hadoopConfDir) {
        var yarnConfiguration = new Configuration(false);
        yarnConfiguration.addResource(new Path(hadoopConfDir, "core-site.xml"));
        yarnConfiguration.addResource(new Path(hadoopConfDir, "yarn-site.xml"));
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
