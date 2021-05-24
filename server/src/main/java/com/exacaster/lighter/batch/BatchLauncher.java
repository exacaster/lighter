package com.exacaster.lighter.batch;

import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.spark.SparkApp;
import javax.inject.Singleton;

@Singleton
public class BatchLauncher implements OnBatchCreate {

    private final Backend backend;

    public BatchLauncher(Backend backend) {
        this.backend = backend;
    }

    @Override
    public void onBatchCreate(Batch batch) {
        var app = new SparkApp(backend.getSubmitParamas(batch));
        app.launch();
    }
}
