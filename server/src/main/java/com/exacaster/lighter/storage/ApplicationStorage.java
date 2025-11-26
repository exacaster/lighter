package com.exacaster.lighter.storage;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public interface ApplicationStorage {

    Optional<Application> findApplication(String internalApplicationId);

    List<Application> findApplications(EnumSet<ApplicationType> types, Integer from, Integer size);

    void deleteApplication(String internalApplicationId);

    Application saveApplication(Application application);

    Application insertApplication(Application application) throws ApplicationAlreadyExistsException;

    List<Application> findApplicationsByStates(ApplicationType type, List<ApplicationState> states, SortOrder order,
            Integer from, Integer size);

    List<Application> findAllApplications(ApplicationType type);

    List<Application> findFinishedApplicationsOlderThan(ApplicationType type, List<ApplicationState> states, LocalDateTime cutoffDate, Integer limit);

    void hardDeleteApplication(String internalApplicationId);
}
