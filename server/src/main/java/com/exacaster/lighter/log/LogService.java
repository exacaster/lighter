package com.exacaster.lighter.log;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.storage.LogStorage;
import java.util.Optional;
import jakarta.inject.Singleton;

@Singleton
public class LogService {

    private final LogStorage logStorage;
    private final Backend backend;

    public LogService(LogStorage logStorage, Backend backend) {
        this.logStorage = logStorage;
        this.backend = backend;
    }

    public Optional<Log> fetch(String applicationId) {
        return logStorage.findApplicationLog(applicationId);
    }

    public void save(Log log) {
        logStorage.saveApplicationLog(log);
    }

    public Optional<Log> fetchLive(Application application) {
        return backend.getLogs(application);
    }
}
