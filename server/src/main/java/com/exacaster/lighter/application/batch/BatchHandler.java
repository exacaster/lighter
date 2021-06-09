package com.exacaster.lighter.application.batch;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.spark.SparkApp;
import com.exacaster.lighter.spark.SubmitException;
import io.micronaut.scheduling.annotation.Scheduled;
import java.io.IOException;
import javax.inject.Singleton;
import org.slf4j.Logger;

@Singleton
public class BatchHandler {
    private static final Logger LOG = getLogger(BatchHandler.class);

    private final Backend backend;
    private final BatchService batchService;
    private final LogService logService;

    public BatchHandler(Backend backend, BatchService batchService, LogService logService) {
        this.backend = backend;
        this.batchService = batchService;
        this.logService = logService;
    }

    public LaunchResult launch(Application application) {
        var app = new SparkApp(application.getSubmitParams());
        try {
            app.launch(backend.getSubmitConfiguration(application));
        } catch (SubmitException e) {
            LOG.error("Error launching", e);
            return new LaunchResult(ApplicationState.ERROR, e);
        }
        return new LaunchResult(ApplicationState.STARTING, null);
    }

    @Scheduled(fixedRate = "1m")
    public void processScheduledBatches() {
        batchService.fetchByState(ApplicationState.NOT_STARTED)
                .forEach(batch -> {
                    LOG.info("Launching {}", batch);
                    var state = launch(batch);
                    batchService.update(ApplicationBuilder.builder(batch).setState(state.getState()).build());
                    if (state.getException() != null) {
                        logService.save(new Log(batch.getId(), state.getException().toString()));
                    }
                });
    }

    @Scheduled(fixedRate = "2m")
    public void processNonFinalBatches() {
        batchService.fetchNonFinished()
                .forEach(batch -> {
                    backend.getInfo(batch.getId()).ifPresentOrElse(info -> {
                        LOG.info("Tracking {}, info: {}", batch, info);
                        if (info.getState().isComplete()) {
                            backend.getLogs(batch.getId()).ifPresent(logService::save);
                        }
                        batchService.update(ApplicationBuilder.builder(batch)
                                .setState(info.getState())
                                .setAppId(info.getApplicationId())
                                .build());
                    }, () -> LOG.info("No info for {}", batch));
                });
    }


}
