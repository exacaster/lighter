package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.spark.SubmitParams;
import com.exacaster.lighter.storage.ApplicationStorage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class SessionService {

    private final ApplicationStorage applicationStorage;
    private final Backend backend;

    public SessionService(ApplicationStorage applicationStorage, Backend backend) {
        this.applicationStorage = applicationStorage;
        this.backend = backend;
    }

    public List<Application> fetch(Integer from, Integer size) {
        return applicationStorage.findApplications(ApplicationType.SESSION, from, size);
    }

    public Application createStatement(SubmitParams params) {
        var entity = ApplicationBuilder.builder()
                .setId(UUID.randomUUID().toString())
                .setType(ApplicationType.SESSION)
                .setState(ApplicationState.NOT_STARTED)
                .setSubmitParams(params)
                .setCreatedAt(LocalDateTime.now())
                .build();
        return applicationStorage.saveApplication(entity);
    }

    public Optional<Application> fetchOne(String id) {
        return applicationStorage.findApplication(id);
    }

    public void deleteOne(String id) {
        this.fetchOne(id).ifPresent(app -> {
            backend.kill(app);
            applicationStorage.deleteApplication(id);
        });
    }

    public List<Statement> getStatements(String id) {
        return null;
    }

    public Statement createStatement(String id, Statement statement) {
        return null;
    }

    public Statement getStatement(String id, String statementId) {
        return null;
    }

    public Statement cancelStatement(String id, String statementId) {
        return null;
    }

    public List<String> runStatement(String id) {
        return null;
    }
}
