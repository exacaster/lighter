package com.exacaster.lighter.application.sessions;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.application.SubmitParams;
import static com.exacaster.lighter.application.sessions.SessionUtils.adjustState;
import com.exacaster.lighter.application.sessions.exceptions.InvalidSessionStateException;
import com.exacaster.lighter.application.sessions.exceptions.SessionLimitExceededException;
import com.exacaster.lighter.application.sessions.processors.StatementHandler;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.rest.SessionParams;
import com.exacaster.lighter.storage.ApplicationStorage;
import com.exacaster.lighter.storage.SortOrder;
import com.exacaster.lighter.storage.StatementStorage;

import jakarta.inject.Singleton;

@Singleton
public class SessionService {

    public static final EnumSet<ApplicationType> SESSIONS = EnumSet.of(ApplicationType.SESSION, ApplicationType.PERMANENT_SESSION);
    private final ApplicationStorage applicationStorage;
    private final StatementStorage statementStorage;
    private final Backend backend;
    private final StatementHandler statementHandler;
    private final AppConfiguration appConfiguration;

    public SessionService(ApplicationStorage applicationStorage,
                          StatementStorage statementStorage, Backend backend,
                          StatementHandler statementHandler,
                          AppConfiguration appConfiguration) {
        this.applicationStorage = applicationStorage;
        this.statementStorage = statementStorage;
        this.backend = backend;
        this.statementHandler = statementHandler;
        this.appConfiguration = appConfiguration;
    }

    public List<Application> fetch(Integer from, Integer size) {
        return applicationStorage.findApplications(SESSIONS, from, size);
    }

    public Application createSession(SessionParams sessionParams) {
        return createSession(UUID.randomUUID().toString(), sessionParams);
    }

    public Application createSession(String sessionId, SessionParams sessionParams) {
        if (Boolean.TRUE.equals(sessionParams.getPermanent())) {
            return createPermanentSession(sessionId, sessionParams);
        }
        return createRegularSession(sessionId, sessionParams);
    }

    public Application createPermanentSession(String sessionId, SubmitParams params) {
        validateSessionLimit(ApplicationType.PERMANENT_SESSION);
        return createSession(params, sessionId, ApplicationType.PERMANENT_SESSION);
    }

    private void validateSessionLimit(ApplicationType type) {
        if (!SESSIONS.contains(type)) {
            return;
        }

        var maxRunning = appConfiguration.getMaxRunningSessions();
        if (maxRunning == null) {
            return;
        }

        var sessionCount = applicationStorage.findApplications(SESSIONS, 0, maxRunning).size();
        if (sessionCount >= maxRunning) {
            throw new SessionLimitExceededException(maxRunning);
        }
    }

    private Application createRegularSession(String sessionId, SubmitParams params) {
        validateSessionLimit(ApplicationType.SESSION);
        return createSession(params, sessionId, ApplicationType.SESSION);
    }

    private Application createSession(SubmitParams params, String sessionId, ApplicationType applicationType) {
        var name = ofNullable(params.getName())
                .orElseGet(() -> "session_" + UUID.randomUUID());
        var submitParams = params
                .withNameAndFile(name, backend.getSessionJobResources());
        var now = LocalDateTime.now();
        var entity = ApplicationBuilder.builder()
                .setId(sessionId)
                .setType(applicationType)
                .setState(ApplicationState.NOT_STARTED)
                .setSubmitParams(submitParams)
                .setCreatedAt(now)
                .setContactedAt(now)
                .build();
        return applicationStorage.insertApplication(entity);
    }

    protected List<Application> fetchRunningSession() {
        return applicationStorage
                .findApplicationsByStates(ApplicationType.SESSION, ApplicationState.runningStates(), SortOrder.ASC, 0, Integer.MAX_VALUE);
    }

    public List<Application> fetchByState(ApplicationState state, SortOrder order, Integer limit) {
        return applicationStorage.findApplicationsByStates(ApplicationType.SESSION, List.of(state), order, 0, limit);
    }

    public Optional<Application> fetchOne(String id, boolean liveStatus) {
        return applicationStorage.findApplication(id)
                .map(app -> {
                    if (!app.getState().isComplete() && liveStatus) {
                        return backend.getInfo(app)
                                .map(info -> {
                                    var hasWaiting = statementHandler.hasWaitingStatement(app);
                                    var state = adjustState(hasWaiting, info.state());
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
        this.fetchOne(id)
                .filter(application -> SESSIONS.contains(application.getType()))
                .ifPresent(this::deleteOne);
    }

    protected void deleteOne(Application app) {
        backend.kill(app);
        applicationStorage.deleteApplication(app.getId());
    }

    public void killOne(Application app) {
        backend.kill(app);
        applicationStorage.saveApplication(ApplicationBuilder.builder(app).setState(ApplicationState.KILLED).build());
    }

    public Optional<Statement> createStatement(String id, Statement statement) {
        return this.fetchOne(id, true).map(application -> {
            if (!application.getState().isComplete()) {
                return statementHandler.processStatement(application.getId(), statement);
            } else {
                throw new InvalidSessionStateException(application.getState());
            }
        });
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

    public boolean isActive(Application application) {
        return statementHandler.hasWaitingStatement(application);
    }

    public Map<String, Application> fetchAllPermanentSessions() {
        return applicationStorage.findAllApplications(ApplicationType.PERMANENT_SESSION).stream()
                .collect(Collectors.toMap(Application::getId, Function.identity()));
    }

    protected void deletePermanentSession(String id) {
        applicationStorage.findApplication(id).ifPresent(backend::kill);
        applicationStorage.hardDeleteApplication(id);
    }

}
