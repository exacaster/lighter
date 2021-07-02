package com.exacaster.lighter.application.batch;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.spark.SparkApp;
import io.micronaut.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import org.slf4j.Logger;

@Singleton
public class BatchHandler {

    private static final Logger LOG = getLogger(BatchHandler.class);

    private final Backend backend;
    private final BatchService batchService;
    private final LogService logService;
    private final AppConfiguration appConfiguration;

    public BatchHandler(Backend backend, BatchService batchService, LogService logService, AppConfiguration appConfiguration) {
        this.backend = backend;
        this.batchService = batchService;
        this.logService = logService;
        this.appConfiguration = appConfiguration;
    }

    public void launch(Application application, Consumer<Throwable> errorHandler) {
        var app = new SparkApp(application.getSubmitParams(), errorHandler);
        app.launch(backend.getSubmitConfiguration(application));
    }

    @Scheduled(fixedRate = "1m")
    @Transactional
    public void processScheduledBatches() {
        var emptySlots = countEmptySlots();
        LOG.info("Processing scheduled batches, found empty slots: {}", emptySlots);
        batchService.fetchByState(ApplicationState.NOT_STARTED, emptySlots)
                .forEach(batch -> {
                    LOG.info("Launching {}", batch);
                    batchService.update(ApplicationBuilder.builder(batch)
                            .setState(ApplicationState.STARTING)
                            .setContactedAt(LocalDateTime.now())
                            .build());
                    launch(batch, error -> {
                        var appId = backend.getInfo(batch).map(ApplicationInfo::getApplicationId)
                                .orElse(null);
                        batchService.update(
                                ApplicationBuilder.builder(batch)
                                        .setState(ApplicationState.ERROR)
                                        .setAppId(appId)
                                        .setContactedAt(LocalDateTime.now())
                                        .build());

                        backend.getLogs(batch).ifPresentOrElse(
                                logService::save,
                                () -> logService.save(new Log(batch.getId(), error.toString()))
                        );
                    });
                });
    }

    @Scheduled(fixedRate = "2m")
    @Transactional
    public void processRunningBatches() {
        batchService.fetchRunning()
                .forEach(batch ->
                        backend.getInfo(batch).ifPresentOrElse(
                                info -> trackStatus(batch, info),
                                () -> checkZombie(batch)
                        )
                );
    }

    private void trackStatus(Application batch, com.exacaster.lighter.application.ApplicationInfo info) {
        LOG.info("Tracking {}, info: {}", batch, info);
        batchService.update(ApplicationBuilder.builder(batch)
                .setState(info.getState())
                .setContactedAt(LocalDateTime.now())
                .setAppId(info.getApplicationId())
                .build());

        if (info.getState().isComplete()) {
            backend.getLogs(batch).ifPresent(logService::save);
        }
    }

    private Integer countEmptySlots() {
        return Math.max(this.appConfiguration.getMaxRunningJobs() - this.batchService.fetchRunning().size(), 0);
    }

    private void checkZombie(Application batch) {
        LOG.info("No info for {}", batch);
        if (batch.getContactedAt() != null && batch.getContactedAt().isBefore(LocalDateTime.now().minusMinutes(30))) {
            LOG.info("Assuming zombie ({})", batch.getId());
            batchService.update(ApplicationBuilder.builder(batch)
                    .setState(ApplicationState.ERROR)
                    .build());
            logService.save(new Log(batch.getId(),
                    "Application was not reachable for 10 minutes, so we assume something went wrong"));
        }
    }


}
