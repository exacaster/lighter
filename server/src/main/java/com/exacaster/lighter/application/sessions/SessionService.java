package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.application.sessions.processors.python.SessionIntegration;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.spark.SubmitParams;
import com.exacaster.lighter.storage.ApplicationStorage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class SessionService {

    private final ApplicationStorage applicationStorage;
    private final Backend backend;
    private final SessionIntegration entrypoint;
    private final String shellFilePath;

    public SessionService(ApplicationStorage applicationStorage, Backend backend, SessionIntegration entrypoint) {
        this.applicationStorage = applicationStorage;
        this.backend = backend;
        this.entrypoint = entrypoint;

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("shell_wrapper.py").getFile());
        this.shellFilePath = file.getAbsolutePath();
    }

    public List<Application> fetch(Integer from, Integer size) {
        return applicationStorage.findApplications(ApplicationType.SESSION, from, size);
    }

    public Application createSession(SubmitParams params) {
        var submitParams = params.withNameAndFile("session_" + UUID.randomUUID(), "file://" + shellFilePath);
        var entity = ApplicationBuilder.builder()
                .setId(UUID.randomUUID().toString())
                .setType(ApplicationType.SESSION)
                .setState(ApplicationState.NOT_STARTED)
                .setSubmitParams(submitParams)
                .setCreatedAt(LocalDateTime.now())
                .build();
        return applicationStorage.saveApplication(entity);
    }

    public List<Application> fetchRunning() {
        return applicationStorage
                .findApplicationsByStates(ApplicationType.SESSION, ApplicationState.runningStates(), Integer.MAX_VALUE);
    }

    public List<Application> fetchByState(ApplicationState state, Integer limit) {
        return applicationStorage.findApplicationsByStates(ApplicationType.SESSION, List.of(state), limit);
    }

    public Application update(Application application) {
        return applicationStorage.saveApplication(application);
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

    public Statement createSession(String id, Statement statement) {
        return entrypoint.processStatement(id, statement);
    }

    public Statement getStatement(String id, String statementId) {
        return entrypoint.getStatement(id, statementId);
    }

    public Statement cancelStatement(String id, String statementId) {
        return entrypoint.cancelStatement(id, statementId);
    }
}
