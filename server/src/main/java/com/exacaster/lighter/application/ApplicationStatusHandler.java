package com.exacaster.lighter.application;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.storage.ApplicationStorage;

import java.time.LocalDateTime;
import java.util.function.Function;

import jakarta.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

@Singleton
public class ApplicationStatusHandler {

    private static final Logger LOG = getLogger(ApplicationStatusHandler.class);

    private final ApplicationStorage applicationStorage;
    private final Backend backend;
    private final LogService logService;
    private final AppConfiguration conf;

    public ApplicationStatusHandler(ApplicationStorage applicationStorage, Backend backend, LogService logService, AppConfiguration conf) {
        this.applicationStorage = applicationStorage;
        this.backend = backend;
        this.logService = logService;
        this.conf = conf;
    }

    public Application processApplicationStarting(Application application) {
        return applicationStorage.saveApplication(ApplicationBuilder.builder(application)
                .setState(ApplicationState.STARTING)
                .setContactedAt(LocalDateTime.now())
                .build());
    }

    public ApplicationState processApplicationRunning(Application application) {
        return this.processApplicationRunning(application, Function.identity());
    }

    public ApplicationState processApplicationRunning(Application application, Function<ApplicationInfo, ApplicationInfo> infoTransformer) {
        return backend.getInfo(application)
                .map(info -> {
                    var transformedInfo = infoTransformer.apply(info);
                    return trackStatus(application, transformedInfo);
                })
                .orElseGet(() -> checkZombie(application));
    }

    public void processApplicationError(Application application, Throwable error) {
        LOG.warn("Marking application {} failed because of error", application.getId(), error);
        var appId = backend.getInfo(application).map(ApplicationInfo::applicationId)
                .orElse(null);
        applicationStorage.saveApplication(
                ApplicationBuilder.builder(application)
                        .setState(ApplicationState.ERROR)
                        .setAppId(appId)
                        .setContactedAt(LocalDateTime.now())
                        .build());

        backend.getLogs(application).ifPresentOrElse(
                logService::save,
                () -> logService.save(new Log(application.getId(), ExceptionUtils.getStackTrace(error)))
        );
    }

    private ApplicationState trackStatus(Application app, ApplicationInfo info) {
        LOG.info("Tracking {}, info: {}", app, info);
        applicationStorage.saveApplication(ApplicationBuilder.builder(app)
                .setState(info.state())
                .setContactedAt(LocalDateTime.now())
                .setAppId(info.applicationId())
                .build());

        if (info.state().isComplete()) {
            backend.getLogs(app).ifPresent(logService::save);
        }

        return info.state();
    }

    private ApplicationState checkZombie(Application app) {
        LOG.info("No info for {}", app);
        if (app.getContactedAt() != null && app.getContactedAt().isBefore(LocalDateTime.now().minus(conf.getZombieInterval()))) {
            LOG.info("Assuming zombie ({})", app.getId());
            applicationStorage.saveApplication(ApplicationBuilder.builder(app)
                    .setState(ApplicationState.ERROR)
                    .build());
            logService.save(new Log(app.getId(),
                    "Application was not reachable for " + conf.getZombieInterval().toMinutes() + " minutes, so we assume something went wrong"));
            return ApplicationState.ERROR;
        }
        return app.getState();
    }
}
