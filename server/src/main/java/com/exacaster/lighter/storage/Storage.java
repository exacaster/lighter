package com.exacaster.lighter.storage;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.log.Log;
import java.util.List;
import java.util.Optional;

public interface Storage {
    Optional<Application> findApplication(String internalApplicationId);
    List<Application> findApplications(ApplicationType type, Integer from, Integer size);
    void deleteApplication(String internalApplicationId);
    Application saveApplication(Application application);
    List<Application> findApplicationsByStates(ApplicationType type, List<ApplicationState> states);

    Optional<Log> findApplicationLog(String internalApplicationId);
    Log saveApplicationLog(Log log);
}
