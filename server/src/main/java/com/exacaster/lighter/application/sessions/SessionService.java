package com.exacaster.lighter.application.sessions;

import static com.exacaster.lighter.application.sessions.SessionUtils.adjustState;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.application.sessions.processors.StatementHandler;
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
    private final StatementHandler statementHandler;

    public SessionService(ApplicationStorage applicationStorage, Backend backend, StatementHandler statementHandler) {
        this.applicationStorage = applicationStorage;
        this.backend = backend;
        this.statementHandler = statementHandler;
    }

    public List<Application> fetch(Integer from, Integer size) {
        return applicationStorage.findApplications(ApplicationType.SESSION, from, size);
    }

    public Application createSession(SubmitParams params) {
        var submitParams = params.withNameAndFile("session_" + UUID.randomUUID(), backend.getSessionJobResources());
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


    public Optional<Application> fetchOne(String id, boolean liveStatus) {
        return applicationStorage.findApplication(id)
                .map(app -> {
                    if (app.getState().isComplete() && liveStatus) {
                        return backend.getInfo(app)
                                .map(info -> {
                                    var hasWaiting = statementHandler.hasWaitingStatement(app);
                                    var state = adjustState(!hasWaiting, info.getState());
                                    return ApplicationBuilder.builder(app).setState(state).build();
                                })
                                .orElse(app);
                    }
                    return app;
                });

    }

    public Optional<Application> fetchOne(String id) {
        return this.fetchOne(id, false);
    }

    public void deleteOne(String id) {
        this.fetchOne(id).ifPresent(app -> {
            backend.kill(app);
            applicationStorage.deleteApplication(id);
        });
    }

    public void killOne(Application app) {
        backend.kill(app);
        applicationStorage.saveApplication(ApplicationBuilder.builder(app).setState(ApplicationState.KILLED).build());
    }

    public Statement createStatement(String id, Statement statement) {
        return statementHandler.processStatement(id, statement);
    }

    public Statement getStatement(String id, String statementId) {
        return statementHandler.getStatement(id, statementId);
    }

    public Statement cancelStatement(String id, String statementId) {
        return statementHandler.cancelStatement(id, statementId);
    }
}
