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

import static java.util.Optional.ofNullable;
import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class SessionHandler {

    private static final Logger LOG = getLogger(SessionHandler.class);

    private final SessionService sessionService;
    private final Backend backend;
    private final StatementHandler statementStatusChecker;
    private final ApplicationStatusHandler statusTracker;
    private final AppConfiguration appConfiguration;

    public SessionHandler(SessionService sessionService,
            Backend backend,
            StatementHandler statementStatusChecker,
            ApplicationStatusHandler statusTracker,
            AppConfiguration appConfiguration) {
        this.sessionService = sessionService;
        this.backend = backend;
        this.statementStatusChecker = statementStatusChecker;
        this.statusTracker = statusTracker;
        this.appConfiguration = appConfiguration;
    }

    public Waitable launch(Application application, Consumer<Throwable> errorHandler) {
        var app = backend.prepareSparkApplication(application, appConfiguration.getSessionDefaultConf(), errorHandler);
        return app.launch();
    }

    @SchedulerLock(name = "keepPermanentSession", lockAtLeastFor = "1m")
    @Scheduled(fixedRate = "1m")
    public void keepPermanentSessions() throws InterruptedException {
        assertLocked();
        LOG.info("Start provisioning permanent sessions.");
        for (var sessionConf : appConfiguration.getSessionConfiguration().getPermanentSessions()) {
            var session = sessionService.fetchOne(sessionConf.getId());
            if (session.map(Application::getState).filter(this::running).isEmpty() ||
                    session.flatMap(backend::getInfo).map(ApplicationInfo::getState).filter(this::running).isEmpty()) {
                LOG.info("Permanent session {} needs to be (re)started.", sessionConf.getId());
                var sessionToLaunch = sessionService.createSession(
                        sessionConf.getSubmitParams(),
                        sessionConf.getId()
                );

                sessionService.deleteOne(sessionToLaunch);
                launchSession(sessionToLaunch).waitCompletion();
                LOG.info("Permanent session {} (re)started.", sessionConf.getId());
            }
        }
        LOG.info("End provisioning permanent sessions.");
    }

    @SchedulerLock(name = "keepPermanentSession", lockAtLeastFor = "1m")
    @Scheduled(fixedRate = "1m")
    public void keepPermanentSessions2() throws InterruptedException {
        assertLocked();
        LOG.info("Start provisioning permanent sessions.");

        final var allPermanentSessions = getPermanentSessionToCheck();

        for (var perm : allPermanentSessions) {
            var session = sessionService.fetchOne(perm.getSessionId());
            if (session.map(Application::getState).filter(this::running).isEmpty() ||
                    session.flatMap(backend::getInfo).map(ApplicationInfo::getState).filter(this::running).isEmpty()) {
                LOG.info("Permanent session {} needs to be (re)started.", perm.getSessionId());
                var sessionToLaunch = sessionService.createPermanentSession(
                        perm.getSessionId(),
                        perm.getSubmitParams()
                );

                sessionService.deleteOne(sessionToLaunch);
                launchSession(sessionToLaunch).waitCompletion();
                LOG.info("Permanent session {} (re)started.", perm.getSessionId());
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

        //TODO he we will add checking if session in db is not marked as deleted
        final var intersection = Sets.intersection(configurationPermanentSessions.keySet(), dbPermanentSessions.keySet()).stream()
                .map(id -> new PermanentSessionParam(id, dbPermanentSessions.get(id).getSubmitParams()));

        final var fromStorageOnly = Sets.difference(dbPermanentSessions.keySet(), configurationPermanentSessions.keySet()).stream().map(
                id -> new PermanentSessionParam(id, dbPermanentSessions.get(id).getSubmitParams())
        );

        return Stream.concat(fromStorageOnly, Stream.concat(fromYamlOnly, intersection)).collect(Collectors.toList());

    }

    private static class PermanentSessionParam {
        private final String sessionId;
        private final SubmitParams submitParams;

        public PermanentSessionParam(String sessionId, SubmitParams submitParams) {
            this.sessionId = sessionId;
            this.submitParams = submitParams;
        }

        public String getSessionId() {
            return sessionId;
        }

        public SubmitParams getSubmitParams() {
            return submitParams;
        }
    }

    @SchedulerLock(name = "processScheduledSessions")
    @Scheduled(fixedRate = "${lighter.session.schedule-interval}")
    public void processScheduledSessions() throws InterruptedException {
        assertLocked();
        var waitables = sessionService.fetchByState(ApplicationState.NOT_STARTED, SortOrder.ASC, 10).stream()
                .map(this::launchSession)
                .collect(Collectors.toList());

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
        var running = sessionService.fetchRunning();

        var idleAndRunning = running.stream()
                .collect(Collectors.groupingBy(statementStatusChecker::hasWaitingStatement));

        selfOrEmpty(idleAndRunning.get(false)).forEach(statusTracker::processApplicationIdle);
        selfOrEmpty(idleAndRunning.get(true)).forEach(statusTracker::processApplicationRunning);
    }

    @SchedulerLock(name = "handleTimeoutSessions")
    @Scheduled(fixedRate = "10m")
    public void handleTimeout() {
        assertLocked();
        var sessionConfiguration = appConfiguration.getSessionConfiguration();
        var timeoutInterval = sessionConfiguration.getTimeoutInterval();
        if (timeoutInterval != null && !timeoutInterval.isZero()) {
            sessionService.fetchRunning()
                    .stream()
                    .filter(s -> isNotPermanent(sessionConfiguration, s))
                    .filter(s -> sessionConfiguration.shouldTimeoutActive() || !sessionService.isActive(s))
                    .filter(s -> sessionService.lastUsed(s.getId()).isBefore(LocalDateTime.now().minus(timeoutInterval)))
                    .peek(s -> LOG.info("Killing because of timeout {}, session: {}", timeoutInterval, s))
                    .forEach(sessionService::killOne);
        }

    }

    private boolean isNotPermanent(AppConfiguration.SessionConfiguration sessionConfiguration, Application session) {
        return sessionConfiguration.getPermanentSessions().stream()
                .noneMatch(conf -> conf.getId().equals(session.getId()));
    }

    private <T> List<T> selfOrEmpty(List<T> list) {
        return ofNullable(list).orElse(List.of());
    }

    private boolean running(ApplicationState state) {
        return !state.isComplete();
    }
}
