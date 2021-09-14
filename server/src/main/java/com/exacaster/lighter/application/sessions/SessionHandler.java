package com.exacaster.lighter.application.sessions;

import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationStatusHandler;
import com.exacaster.lighter.application.sessions.processors.StatementHandler;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.spark.SparkApp;
import io.micronaut.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import javax.transaction.Transactional;
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

    public void launch(Application application, Consumer<Throwable> errorHandler) {
        var app = new SparkApp(application.getSubmitParams(), errorHandler);
        app.launch(backend.getSubmitConfiguration(application));
    }


    @Scheduled(fixedRate = "1m")
    @Transactional
    public void processScheduledBatches() {
        sessionService.fetchByState(ApplicationState.NOT_STARTED, 10)
                .forEach(session -> {
                    LOG.info("Launching {}", session);
                    statusTracker.processApplicationStarting(session);
                    launch(session, error -> statusTracker.processApplicationError(session, error));
                });
    }

    @Scheduled(fixedRate = "2m")
    @Transactional
    public void trackRunning() {
        var running = sessionService.fetchRunning();

        var idleAndRunning = running.stream()
                .collect(Collectors.groupingBy(statementStatusChecker::hasWaitingStatement));

        selfOrEmpty(selfOrEmpty(idleAndRunning.get(false))).forEach(statusTracker::processApplicationIdle);

        statusTracker.processApplicationsRunning(
                selfOrEmpty(idleAndRunning.get(true))
        );
    }

    @Scheduled(fixedRate = "10m")
    public void handleTimeout() {
        var timeout = appConfiguration.getSessionConfiguration().getTimeoutMinutes();
        if (timeout != null) {
            sessionService.fetchRunning()
                    .stream()
                    .filter(s -> s.getContactedAt().isBefore(LocalDateTime.now().minusMinutes(timeout)))
                    .peek(s -> LOG.info("Killing because of timeout {}, session: {}", timeout, s))
                    .forEach(sessionService::killOne);
        }

    }

    private <T> List<T> selfOrEmpty(List<T> list) {
        return ofNullable(list).orElse(List.of());
    }
}
