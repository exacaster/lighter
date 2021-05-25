package com.exacaster.lighter.backend;

import com.exacaster.lighter.batch.Batch;
import java.util.Map;
import java.util.Optional;

public interface Backend {

    void configure(Map<String, String> configs);

    Optional<BatchInfo> getInfo(String appIdentifier);

    Optional<String> getLogs(String appIdentifier);

    default Map<String, String> getSubmitConfiguration(Batch batch) {
        return Map.of();
    }
}
