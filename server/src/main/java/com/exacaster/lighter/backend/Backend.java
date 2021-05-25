package com.exacaster.lighter.backend;

import com.exacaster.lighter.batch.Batch;
import com.exacaster.lighter.spark.SubmitParams;
import java.util.Map;
import java.util.Optional;

public interface Backend {

    void configure(Map<String, String> configs);

    Optional<BatchInfo> getInfo(String appIdentifier);

    default Map<String, String> getSubmitConfiguration(Batch batch) {
        return Map.of();
    }
}
