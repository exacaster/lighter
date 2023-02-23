package com.exacaster.lighter.application.batch;

import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;
import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationStatusHandler;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.concurrency.Waitable;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.storage.SortOrder;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.javacrumbs.shedlock.micronaut.SchedulerLock;
import org.slf4j.Logger;

@Singleton
public class BatchHandler {

    private static final Logger LOG = getLogger(BatchHandler.class);

    private final Backend backend;
    private final BatchService batchService;
    private final AppConfiguration appConfiguration;
    private final ApplicationStatusHandler statusTracker;

    public BatchHandler(Backend backend, BatchService batchService, AppConfiguration appConfiguration,
                        ApplicationStatusHandler statusTracker) {
        this.backend = backend;
        this.batchService = batchService;
        this.appConfiguration = appConfiguration;
        this.statusTracker = statusTracker;
    }

    public Waitable launch(Application application, Consumer<Throwable> errorHandler) {
        var app = backend.prepareSparkApplication(application, appConfiguration.getBatchDefaultConf(), errorHandler);
        return app.launch();
    }

    @SchedulerLock(name = "processScheduledBatches")
    @Scheduled(fixedDelay = "30s")
    public void processScheduledBatches() throws InterruptedException {
        assertLocked();
        var maxSlotsForNewJobs = getMaxSlotsForNewJobs();
        var batchesToStart = batchService.fetchByState(ApplicationState.NOT_STARTED, SortOrder.ASC, 0, maxSlotsForNewJobs)
                .stream()
                .map(batch -> {
                    LOG.info("Launching {}", batch);
                    statusTracker.processApplicationStarting(batch);
                    return launch(batch, error -> statusTracker.processApplicationError(batch, error));
                })
                .collect(Collectors.toList());
        LOG.info("Triggered {} new batch jobs. Waiting launches to complete.", batchesToStart.size());
        for (var batchToStart : batchesToStart) {
            batchToStart.waitCompletion();
        }
    }

    private Integer getMaxSlotsForNewJobs() {
        var numberOfJobsRunning = this.batchService.fetchRunning().size();

        var maxAvailableSlots = Math.max(this.appConfiguration.getMaxRunningJobs() - numberOfJobsRunning, 0);
        var maxSlotsForNewJobs = Math.min(
                this.appConfiguration.getMaxStartingJobs(),
                maxAvailableSlots
        );

        LOG.info("Processing scheduled batches. Running jobs: {}/{}. {} slots can be used for new jobs.",
                numberOfJobsRunning,
                this.appConfiguration.getMaxRunningJobs(),
                maxSlotsForNewJobs);

        return maxSlotsForNewJobs;
    }

    @SchedulerLock(name = "trackRunning")
    @Scheduled(fixedDelay = "30s")
    public void trackRunning() throws InterruptedException {
        assertLocked();
        var completedCount = batchService.fetchRunning().stream()
                .map(statusTracker::processApplicationRunning)
                .filter(ApplicationState::isComplete)
                .count();
        LOG.info("Completed {} jobs", completedCount);

        // If there are completed jobs, we can launch scheduled jobs immediately
        if (completedCount > 0) {
            this.processScheduledBatches();
        }
    }

}
