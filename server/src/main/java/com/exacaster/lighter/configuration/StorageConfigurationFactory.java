package com.exacaster.lighter.configuration;

import com.exacaster.lighter.storage.InMemoryStorage;
import com.exacaster.lighter.storage.Storage;
import io.micronaut.context.annotation.Factory;
import javax.inject.Singleton;

@Factory
public class StorageConfigurationFactory {

    @Singleton
    public Storage storage() {
        // TODO: LOAD by config
        return new InMemoryStorage(1000, 24);
    }
}
