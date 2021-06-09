package com.exacaster.lighter.log;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.storage.Storage;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public class LogService {

    private final Storage storage;
    private final Backend backend;

    public LogService(Storage storage, Backend backend) {
        this.storage = storage;
        this.backend = backend;
    }

    public Optional<Log> fetch(String internalApplicationId) {
        return storage.findApplicationLog(internalApplicationId);
    }

    public void save(Log log) {
        storage.saveApplicationLog(log);
    }

    public Optional<Log> fetchLive(String id) {
        return backend.getLogs(id);
    }
}
