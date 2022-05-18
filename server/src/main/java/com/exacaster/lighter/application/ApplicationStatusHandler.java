package com.exacaster.lighter.application;

import static com.exacaster.lighter.application.sessions.SessionUtils.adjustState;
import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.storage.ApplicationStorage;
import java.time.LocalDateTime;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

@Singleton
public class ApplicationStatusHandler {

    private static final Logger LOG = getLogger(ApplicationStatusHandler.class);

    private final ApplicationStorage applicationStorage;
    private final Backend backend;
    private final LogService logService;

    public ApplicationStatusHandler(ApplicationStorage applicationStorage, Backend backend, LogService logService) {
        this.applicationStorage = applicationStorage;
        this.backend = backend;
        this.logService = logService;
    }

    public void processApplicationStarting(Application application) {
        applicationStorage.saveApplication(ApplicationBuilder.builder(application)
                .setState(ApplicationState.STARTING)
                .setContactedAt(LocalDateTime.now())
                .build());
    }

    public void processApplicationIdle(Application application) {
        backend.getInfo(application).ifPresentOrElse(
                info -> {
                    var state = adjustState(true, info.getState());
                    var idleInfo = new ApplicationInfo(state, info.getApplicationId());
                    trackStatus(application, idleInfo);
                },
                () -> checkZombie(application)
        );
    }

    public ApplicationState processApplicationRunning(Application app) {
        return backend.getInfo(app)
                .map(info -> trackStatus(app, info))
                .orElseGet(() -> checkZombie(app));
    }

    public void processApplicationError(Application application, Throwable error) {
        // Workaround for case when Spark launcher logs
        // `DEBUG Configuration: Handling deprecation for hive.stats.ndv.error`
        // on org.apache.spark.launcher.OutputRedirector#redirect() it marks all stdout lines
        // containing `error` string as Exceptions.
        if (error.getMessage().contains("hive.stats.ndv.error")) {
            LOG.debug("Skipping", error);
            return;
        }

        LOG.warn("Application {} error occurred", application, error);
        var appId = backend.getInfo(application).map(ApplicationInfo::getApplicationId)
                .orElse(null);
        applicationStorage.saveApplication(
                ApplicationBuilder.builder(application)
                        .setState(ApplicationState.ERROR)
                        .setAppId(appId)
                        .setContactedAt(LocalDateTime.now())
                        .build());

        backend.getLogs(application).ifPresentOrElse(
                logService::save,
                () -> logService.save(new Log(application.getId(), error.toString()))
        );
    }

    private ApplicationState trackStatus(Application app, ApplicationInfo info) {
        LOG.info("Tracking {}, info: {}", app, info);
        applicationStorage.saveApplication(ApplicationBuilder.builder(app)
                .setState(info.getState())
                .setContactedAt(LocalDateTime.now())
                .setAppId(info.getApplicationId())
                .build());

        if (info.getState().isComplete()) {
            backend.getLogs(app).ifPresent(logService::save);
        }

        return info.getState();
    }

    private ApplicationState checkZombie(Application app) {
        LOG.info("No info for {}", app);
        if (app.getContactedAt() != null && app.getContactedAt().isBefore(LocalDateTime.now().minusMinutes(30))) {
            LOG.info("Assuming zombie ({})", app.getId());
            applicationStorage.saveApplication(ApplicationBuilder.builder(app)
                    .setState(ApplicationState.ERROR)
                    .build());
            logService.save(new Log(app.getId(),
                    "Application was not reachable for 10 minutes, so we assume something went wrong"));
            return ApplicationState.ERROR;
        }
        return app.getState();
    }
}
