package com.exacaster.lighter.batch;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.spark.SparkApp;
import io.micronaut.scheduling.annotation.Scheduled;
import java.io.IOException;
import javax.inject.Singleton;
import org.slf4j.Logger;

@Singleton
public class BatchHandler {
    private static final Logger LOG = getLogger(BatchHandler.class);

    private final Backend backend;
    private final BatchService service;

    public BatchHandler(Backend backend, BatchService service) {
        this.backend = backend;
        this.service = service;
    }

    public BatchState launch(Batch batch) {
        var app = new SparkApp(batch.submitParams());
        try {
            app.launch(backend.getSubmitConfiguration(batch));
        } catch (IOException | IllegalArgumentException e) {
            LOG.error("Error launching");
            return BatchState.error;
        }
        return BatchState.starting;
    }

    @Scheduled(fixedRate = "1m")
    public void processScheduledBatches() {
        service.fetchByState(BatchState.not_started)
                .forEach(batch -> {
                    LOG.info("Launching {}", batch);
                    var state = launch(batch);
                    service.update(BatchBuilder.builder(batch).state(state).build());
                });
    }

    @Scheduled(fixedRate = "2m")
    public void processNonFinalBatches() {
        service.fetchNonFinished()
                .forEach(batch -> {
                    backend.getInfo(batch.id()).ifPresentOrElse(info -> {
                        BatchBuilder.builder(batch).state(BatchState.starting).build();
                        LOG.info("Tracking {}, info: {}", batch, info);
                        // TODO: Store logs
                        // TODO: Get appId
                        service.update(BatchBuilder.builder(batch).state(info.state()).build());
                    }, () -> LOG.info("No info for {}", batch));
                });
    }


}
