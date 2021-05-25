package com.exacaster.lighter.batch;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.spark.SparkApp;
import io.micronaut.scheduling.annotation.Scheduled;
import javax.inject.Singleton;

@Singleton
public class BatchLauncher {

    private final Backend backend;
    private final BatchService service;

    public BatchLauncher(Backend backend, BatchService service) {
        this.backend = backend;
        this.service = service;
    }

    public void onBatchCreate(Batch batch) {
        var app = new SparkApp(backend.getSubmitParamas(batch));
        app.launch();
    }

    @Scheduled
    public void processScheduledApps() {
        service.fetchByState(BatchState.not_started)
                .forEach(batch -> {
                    BatchBuilder.builder();
                });
    }
}
