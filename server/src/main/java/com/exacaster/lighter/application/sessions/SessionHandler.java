package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationStatusHandler;
import com.exacaster.lighter.application.SubmitParams;
import com.exacaster.lighter.application.sessions.processors.StatementHandler;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.concurrency.Waitable;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.storage.ApplicationStorage;
import com.exacaster.lighter.storage.SortOrder;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import net.javacrumbs.shedlock.micronaut.SchedulerLock;
import org.apache.hadoop.thirdparty.com.google.common.collect.Sets;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.exacaster.lighter.application.sessions.SessionUtils.adjustState;
import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class SessionHandler {

    private static final Logger LOG = getLogger(SessionHandler.class);

    private final SessionService sessionService;
    private final Backend backend;
    private final StatementHandler statementHandler;
    private final ApplicationStatusHandler statusTracker;
    private final AppConfiguration appConfiguration;
    private final ApplicationStorage applicationStorage;

    public SessionHandler(SessionService sessionService,
                          Backend backend,
                          StatementHandler statementHandler,
                          ApplicationStatusHandler statusTracker,
                          AppConfiguration appConfiguration,
                          ApplicationStorage applicationStorage) {
        this.sessionService = sessionService;
        this.backend = backend;
        this.statementHandler = statementHandler;
        this.statusTracker = statusTracker;
        this.appConfiguration = appConfiguration;
        this.applicationStorage = applicationStorage;
    }

    public Waitable launch(Application application, Consumer<Throwable> errorHandler) {
        var app = backend.prepareSparkApplication(application, appConfiguration.getSessionDefaultConf(), errorHandler);
        return app.launch();
    }

    @SchedulerLock(name = "keepPermanentSession", lockAtLeastFor = "1m")
    @Scheduled(fixedRate = "1m", initialDelay = "2s")
    public void keepPermanentSessions() throws InterruptedException {
        assertLocked();
        LOG.info("Start provisioning permanent sessions.");

        final var allPermanentSessions = getPermanentSessionToCheck();

        for (var perm : allPermanentSessions) {
            var session = sessionService.fetchOne(perm.sessionId());
            if (session.map(Application::getState).filter(this::running).isEmpty() ||
                    session.flatMap(backend::getInfo).map(ApplicationInfo::state).filter(this::running).isEmpty()) {
                LOG.info("Permanent session {} needs to be (re)started.", perm.sessionId());
                sessionService.deletePermanentSession(perm.sessionId);
                var sessionToLaunch = sessionService.createPermanentSession(
                        perm.sessionId(),
                        perm.submitParams()
                );

                launchSession(sessionToLaunch).waitCompletion();
                LOG.info("Permanent session {} (re)started.", perm.sessionId());
            }
        }
        LOG.info("End provisioning permanent sessions.");
    }

    private List<PermanentSessionParam> getPermanentSessionToCheck() {
        final var dbPermanentSessions = sessionService.fetchAllPermanentSessions();

        final var configurationPermanentSessions = appConfiguration.getSessionConfiguration().getPermanentSessions().stream().collect(
                Collectors.toMap(permanentSession -> permanentSession.getId(), Function.identity()));

        final var fromYamlOnly = Sets.difference(configurationPermanentSessions.keySet(), dbPermanentSessions.keySet()).stream().map(
                id -> new PermanentSessionParam(id, configurationPermanentSessions.get(id).getSubmitParams())
        );

        final var intersection = Sets.intersection(configurationPermanentSessions.keySet(), dbPermanentSessions.keySet()).stream()
                .filter(id -> dbPermanentSessions.get(id).isNotDeleted())
                .map(id -> new PermanentSessionParam(id, dbPermanentSessions.get(id).getSubmitParams()));

        final var fromStorageOnly = Sets.difference(dbPermanentSessions.keySet(), configurationPermanentSessions.keySet()).stream().map(
                id -> new PermanentSessionParam(id, dbPermanentSessions.get(id).getSubmitParams())
        );

        return Stream.concat(fromStorageOnly, Stream.concat(fromYamlOnly, intersection)).collect(Collectors.toList());

    }

    @SchedulerLock(name = "processScheduledSessions")
    @Scheduled(fixedRate = "${lighter.session.schedule-interval}")
    public void processScheduledSessions() throws InterruptedException {
        assertLocked();
        var waitables = sessionService.fetchByState(ApplicationState.NOT_STARTED, SortOrder.ASC, 10).stream()
                .map(this::launchSession)
                .toList();

        for (var waitable : waitables) {
            waitable.waitCompletion();
        }
    }

    private Waitable launchSession(Application session) {
        LOG.info("Launching {}", session);
        statusTracker.processApplicationStarting(session);
        return launch(session, error -> statusTracker.processApplicationError(session, error));
    }

    @SchedulerLock(name = "trackRunningSessions", lockAtMostFor = "1m")
    @Scheduled(fixedRate = "${lighter.session.track-running-interval}")
    public void trackRunning() {
        assertLocked();
        var running = sessionService.fetchRunningSession();

        running.forEach(session -> {
            var hasWaiting = statementHandler.hasWaitingStatement(session);
            var status = statusTracker.processApplicationRunning(session, info -> adjustApplicationInfo(info, hasWaiting));
            if (status.isComplete()) {
                statementHandler.cancelStatements(session.getId());
            }
        });
    }

    @SchedulerLock(name = "handleTimeoutSessions")
    @Scheduled(fixedRate = "10m")
    public void handleTimeout() {
        assertLocked();
        var sessionConfiguration = appConfiguration.getSessionConfiguration();
        var timeoutInterval = sessionConfiguration.getTimeoutInterval();
        if (timeoutInterval != null && !timeoutInterval.isZero()) {
            sessionService.fetchRunningSession()
                    .stream()
                    .filter(s -> sessionConfiguration.shouldTimeoutActive() || !sessionService.isActive(s))
                    .filter(s -> sessionService.lastUsed(s.getId()).isBefore(LocalDateTime.now().minus(timeoutInterval)))
                    .peek(s -> LOG.info("Killing because of timeout {}, session: {}", timeoutInterval, s))
                    .forEach(sessionService::killOne);
        }

    }

    @SchedulerLock(name = "cleanupFinishedSessions", lockAtMostFor = "1m")
    @Scheduled(fixedRate = "1m")
    public void cleanupFinishedSessions() {
        assertLocked();
        var stateRetainInterval = appConfiguration.getStateRetainInterval();
        if (stateRetainInterval == null) {
            return;
        }
        
        var cutoffDate = LocalDateTime.now().minus(stateRetainInterval);
        var expiredSessions = sessionService.fetchFinishedSessionsOlderThan(cutoffDate, 100);
        
        expiredSessions.parallelStream().forEach(session -> {
            LOG.info("Deleting {} because it was finished for more than {}", session, stateRetainInterval);
            applicationStorage.hardDeleteApplication(session.getId());
        });
    }

    private ApplicationInfo adjustApplicationInfo(ApplicationInfo info, boolean waiting) {
        var state = adjustState(waiting, info.state());
        return new ApplicationInfo(state, info.applicationId());
    }

    private boolean running(ApplicationState state) {
        return !state.isComplete();
    }

    private record PermanentSessionParam(String sessionId, SubmitParams submitParams) {
    }
}
