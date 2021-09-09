package com.exacaster.lighter.backend;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.log.Log;
import java.util.Map;
import java.util.Optional;

public interface Backend {

    Optional<ApplicationInfo> getInfo(Application application);

    Optional<Log> getLogs(Application application);

    String getSessionJobResources();

    void kill(Application application);

    default Map<String, String> getSubmitConfiguration(Application application) {
        return Map.of();
    }
}
