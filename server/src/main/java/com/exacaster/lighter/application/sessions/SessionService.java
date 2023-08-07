package com.exacaster.lighter.application.sessions;

import static com.exacaster.lighter.application.sessions.SessionUtils.adjustState;
import static java.util.Optional.ofNullable;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.application.sessions.processors.StatementHandler;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.application.SubmitParams;
import com.exacaster.lighter.storage.ApplicationStorage;
import com.exacaster.lighter.storage.SortOrder;
import com.exacaster.lighter.storage.StatementStorage;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class SessionService {

    private final ApplicationStorage applicationStorage;
    private final StatementStorage statementStorage;
    private final Backend backend;
    private final StatementHandler statementHandler;

    public SessionService(ApplicationStorage applicationStorage,
            StatementStorage statementStorage, Backend backend,
            StatementHandler statementHandler) {
        this.applicationStorage = applicationStorage;
        this.statementStorage = statementStorage;
        this.backend = backend;
        this.statementHandler = statementHandler;
    }

    public List<Application> fetch(Integer from, Integer size) {
        return applicationStorage.findApplications(ApplicationType.SESSION, from, size);
    }

    public Application createSession(SubmitParams params) {
        return createSession(params, UUID.randomUUID().toString());
    }

    public Application createSession(SubmitParams params, String sessionId) {
        var name = ofNullable(params.getName())
                .orElseGet(() -> "session_" + UUID.randomUUID());
        var submitParams = params
                .withNameAndFile(name, backend.getSessionJobResources());
        var now = LocalDateTime.now();
        var entity = ApplicationBuilder.builder()
                .setId(sessionId)
                .setType(ApplicationType.SESSION)
                .setState(ApplicationState.NOT_STARTED)
                .setSubmitParams(submitParams)
                .setCreatedAt(now)
                .setContactedAt(now)
                .build();
        return applicationStorage.saveApplication(entity);
    }

    public List<Application> fetchRunning() {
        return applicationStorage
                .findApplicationsByStates(ApplicationType.SESSION, ApplicationState.runningStates(), SortOrder.ASC, 0, Integer.MAX_VALUE);
    }

    public List<Application> fetchByState(ApplicationState state, SortOrder order, Integer limit) {
        return applicationStorage.findApplicationsByStates(ApplicationType.SESSION, List.of(state), order, 0, limit);
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
        this.fetchOne(id).ifPresent(this::deleteOne);
    }

    public void deleteOne(Application app) {
        backend.kill(app);
        applicationStorage.deleteApplication(app.getId());
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

    public Optional<Statement> cancelStatement(String id, String statementId) {
        return statementHandler.cancelStatement(id, statementId);
    }

    public LocalDateTime lastUsed(String id) {
        return statementStorage.findLatest(id)
                .map(Statement::getCreatedAt)
                .orElseGet(() -> fetchOne(id).map(Application::getCreatedAt).orElse(null));
    }

    public List<Statement> getStatements(String id, Integer from, Integer size) {
        return statementStorage.find(id).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }
}
