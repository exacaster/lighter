package com.exacaster.lighter.backend.yarn;

import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.HADOOP_KERBEROS_KEYTAB_LOGIN_AUTORENEWAL_ENABLED;
import static org.apache.hadoop.security.UserGroupInformation.loginUserFromKeytab;

import com.exacaster.lighter.application.ApplicationStatusHandler;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.storage.ApplicationStorage;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.client.api.YarnClient;

@Factory
@Requires(beans = YarnProperties.class)
public class YarnConfigurationFactory {
    private final ApplicationStatusHandler applicationStatusHandler;

    private final ApplicationStorage applicationStorage;

    public YarnConfigurationFactory(ApplicationStatusHandler applicationStatusHandler, ApplicationStorage applicationStorage) {
        this.applicationStatusHandler = applicationStatusHandler;
        this.applicationStorage = applicationStorage;
    }

    @Singleton
    public YarnBackend backend(YarnProperties yarnProperties, AppConfiguration conf,
            @Property(name = "hadoop.conf.dir") String hadoopConfDir) throws IOException {
        var yarnConfiguration = new Configuration(false);
        yarnConfiguration.addResource(new Path(hadoopConfDir, "core-site.xml"));
        yarnConfiguration.addResource(new Path(hadoopConfDir, "yarn-site.xml"));
        var kerberos = yarnProperties.getKerberos();
        if (kerberos != null) {
            yarnConfiguration.setBoolean(HADOOP_KERBEROS_KEYTAB_LOGIN_AUTORENEWAL_ENABLED, true);
            UserGroupInformation.setConfiguration(yarnConfiguration);
            loginUserFromKeytab(kerberos.getPrincipal(), kerberos.getKeytab());
        }
        var yarnClient = YarnClient.createYarnClient();
        yarnClient.init(yarnConfiguration);
        yarnClient.start();
        return new YarnBackend(yarnProperties, yarnClient, conf, applicationStatusHandler, applicationStorage);
    }
}
