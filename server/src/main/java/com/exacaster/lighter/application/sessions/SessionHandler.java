package com.exacaster.lighter.application.sessions;

import static java.util.Optional.ofNullable;
import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;
import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationStatusHandler;
import com.exacaster.lighter.application.sessions.processors.StatementHandler;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.concurrency.Waitable;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.storage.SortOrder;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.javacrumbs.shedlock.micronaut.SchedulerLock;
import org.slf4j.Logger;

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

    @SchedulerLock(name = "processScheduledSessions")
    @Scheduled(fixedRate = "1m")
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

    @SchedulerLock(name = "trackRunningSessions")
    @Scheduled(fixedRate = "2m")
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
        var timeout = sessionConfiguration.getTimeoutMinutes();
        if (timeout != null) {
            sessionService.fetchRunning()
                    .stream()
                    .filter(s -> sessionConfiguration.getPermanentSessions().stream()
                            .noneMatch(conf -> conf.getId().equals(s.getId())))
                    .filter(s -> sessionService.lastUsed(s.getId()).isBefore(LocalDateTime.now().minusMinutes(timeout)))
                    .peek(s -> LOG.info("Killing because of timeout {}, session: {}", timeout, s))
                    .forEach(sessionService::killOne);
        }

    }

    private <T> List<T> selfOrEmpty(List<T> list) {
        return ofNullable(list).orElse(List.of());
    }

    private boolean running(ApplicationState state) {
        return !state.isComplete();
    }
}
