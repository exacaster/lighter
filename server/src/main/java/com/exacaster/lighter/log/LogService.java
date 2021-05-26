package com.exacaster.lighter.log;

import com.exacaster.lighter.storage.Storage;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public class LogService {

    private final Storage storage;

    public LogService(Storage storage) {
        this.storage = storage;
    }

    public Optional<Log> fetch(String internalApplicationId) {
        return storage.findApplicationLog(internalApplicationId);
    }

    public void save(Log log) {
        storage.saveApplicationLog(log);
    }
}
