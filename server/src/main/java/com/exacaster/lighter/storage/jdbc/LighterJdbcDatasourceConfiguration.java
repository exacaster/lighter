package com.exacaster.lighter.storage.jdbc;


import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.jdbc.BasicJdbcConfiguration;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Map;

@Requires(property = "lighter.storage.jdbc.enabled", value = "true")
@Factory
public class LighterJdbcDatasourceConfiguration {

    @Singleton
    @Named("default")
    public DatasourceConfiguration datasourceConfiguration(JdbcConnectionConfiguration configuration) {
        var ds = new DatasourceConfiguration("default");
        ds.setUrl(configuration.url);
        ds.setUsername(configuration.username);
        ds.setPassword(configuration.password);
        ds.setDriverClassName(configuration.driverClassName);
        return ds;
    }

    @Primary
    @Introspected
    @ConfigurationProperties("lighter.storage.jdbc")
    public static class JdbcConnectionConfiguration implements BasicJdbcConfiguration {

        private String url;
        private String password;
        private String username;
        private String driverClassName;

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getConfiguredUrl() {
            return url;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String getConfiguredDriverClassName() {
            return driverClassName;
        }

        @Override
        public String getDriverClassName() {
            return driverClassName;
        }

        @Override
        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        @Override
        public String getConfiguredUsername() {
            return username;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String getConfiguredPassword() {
            return password;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String getConfiguredValidationQuery() {
            return null;
        }

        @Override
        public String getValidationQuery() {
            return null;
        }

        @Override
        public void setDataSourceProperties(Map<String, ?> dsProperties) {
        }
    }


}
