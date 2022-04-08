package com.exacaster.lighter.application.batch;

import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;
import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationStatusHandler;
import com.exacaster.lighter.application.Utils;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.concurrency.Waitable;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.spark.ConfigModifier;
import com.exacaster.lighter.spark.SparkApp;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.javacrumbs.shedlock.micronaut.SchedulerLock;
import org.slf4j.Logger;

@Singleton
public class BatchHandler {

    private static final Logger LOG = getLogger(BatchHandler.class);
    private static final int MAX_SLOTS_PER_ITERATION = 10;

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
        List<ConfigModifier> configModifiers = List.of(
                (current) -> Utils.merge(current, appConfiguration.getBatchDefaultConf()),
                (current) -> backend.getSubmitConfiguration(application, current)
        );
        var app = new SparkApp(application.getSubmitParams(), errorHandler, configModifiers);
        return app.launch();
    }

    @SchedulerLock(name = "processScheduledBatches")
    @Scheduled(fixedRate = "1m")
    public void processScheduledBatches() throws InterruptedException {
        assertLocked();
        var emptySlots = countEmptySlots();
        var slotsToTake = Math.min(MAX_SLOTS_PER_ITERATION, emptySlots);
        LOG.info("Processing scheduled batches, found empty slots: {}, using {}", emptySlots, slotsToTake);
        var waitables = batchService.fetchByState(ApplicationState.NOT_STARTED, slotsToTake)
                .stream()
                .map(batch -> {
                    LOG.info("Launching {}", batch);
                    statusTracker.processApplicationStarting(batch);
                    return launch(batch, error -> statusTracker.processApplicationError(batch, error));
                })
                .collect(Collectors.toList());
        LOG.info("Waiting launches to complete");
        for (var waitable : waitables) {
            waitable.waitCompletion();
        }
    }

    private Integer countEmptySlots() {
        return Math.max(this.appConfiguration.getMaxRunningJobs() - this.batchService.fetchRunning().size(), 0);
    }

    @SchedulerLock(name = "trackRunning")
    @Scheduled(fixedRate = "1m")
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
