package com.exacaster.lighter.storage;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.application.sessions.PermanentSession;

import java.util.List;
import java.util.Optional;

public interface ApplicationStorage {

    Optional<Application> findApplication(String internalApplicationId);

    List<Application> findApplications(ApplicationType type, Integer from, Integer size);

    void deleteApplication(String internalApplicationId);

    Application saveApplication(Application application);

    List<Application> findApplicationsByStates(ApplicationType type, List<ApplicationState> states, SortOrder order,
            Integer from, Integer size);

    List<PermanentSession> findAllPermanentSessions();

    void hardDeleteApplication(String internalApplicationId);
}
