package com.exacaster.lighter.storage.jdbc;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource;
import jakarta.inject.Singleton;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Factory
@Requires(beans = DataSource.class)
public class JdbcLockProvider {

    @Singleton
    public LockProvider lockProvider(DataSource dataSource) {
        DataSource unwrappedDataSource = ((DelegatingDataSource) dataSource).getTargetDataSource();
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(unwrappedDataSource))
                        .usingDbTime()
                        .build()
        );
    }
}
