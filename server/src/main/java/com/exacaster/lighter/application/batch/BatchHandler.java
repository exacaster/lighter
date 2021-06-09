package com.exacaster.lighter.application.batch;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.spark.SparkApp;
import io.micronaut.scheduling.annotation.Scheduled;
import java.util.function.Consumer;
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

    public void launch(Application application, Consumer<Throwable> errorHandler) {
        var app = new SparkApp(application.getSubmitParams(), errorHandler);
        app.launch(backend.getSubmitConfiguration(application));
    }

    @Scheduled(fixedRate = "1m")
    public void processScheduledBatches() {
        batchService.fetchByState(ApplicationState.NOT_STARTED)
                .forEach(batch -> {
                    LOG.info("Launching {}", batch);
                    batchService.update(ApplicationBuilder.builder(batch).setState(ApplicationState.STARTING).build());
                    launch(batch, error -> {
                        batchService.update(ApplicationBuilder.builder(batch).setState(ApplicationState.ERROR).build());
                        logService.save(new Log(batch.getId(), error.toString()));
                    });
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
