package com.exacaster.lighter.backend;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.log.Log;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface Backend {

    Optional<ApplicationInfo> getInfo(Application application);

    Optional<Log> getLogs(Application application);

    String getSessionJobResources();

    void kill(Application application);

    SparkApp prepareSparkApplication(Application application, Map<String, String> configDefaults, Consumer<Throwable> errorHandler);
}
