package com.exacaster.lighter.storage.jdbc;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.micronaut.MicronautJdbcLockProvider;

import javax.sql.DataSource;

@Factory
@Requires(beans = DataSource.class)
public class JdbcLockProvider {

    @Singleton
    public LockProvider lockProvider(DataSource datasource) {
        return new MicronautJdbcLockProvider(datasource);
    }
}
