package com.exacaster.lighter.backend;

import com.exacaster.lighter.log.Log;
import java.util.Map;
import java.util.Optional;

public interface Backend {

    void configure(Map<String, String> configs);

    Optional<ApplicationInfo> getInfo(String internalApplicationId);

    Optional<Log> getLogs(String internalApplicationId);

    default Map<String, String> getSubmitConfiguration(Application application) {
        return Map.of();
    }
}
