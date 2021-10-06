package com.exacaster.lighter.storage;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import net.javacrumbs.shedlock.core.LockProvider;

import javax.sql.DataSource;
import java.util.Optional;

@Factory
@Requires(missingBeans = DataSource.class)
public class DummyLockProvider {

    @Singleton
    public LockProvider lockProvider(DataSource datasource) {
        return lockConfiguration -> Optional.empty();
    }
}
