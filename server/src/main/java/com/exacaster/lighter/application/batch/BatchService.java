package com.exacaster.lighter.application.batch;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.spark.SubmitParams;
import com.exacaster.lighter.storage.ApplicationStorage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class BatchService {

    private final ApplicationStorage applicationStorage;
    private final Backend backend;

    public BatchService(ApplicationStorage applicationStorage, Backend backend) {
        this.applicationStorage = applicationStorage;
        this.backend = backend;
    }

    public List<Application> fetch(Integer from, Integer size) {
        return applicationStorage.findApplications(ApplicationType.BATCH, from, size);
    }

    public Application create(SubmitParams batch) {
        var entity = ApplicationBuilder.builder()
                .setId(UUID.randomUUID().toString())
                .setType(ApplicationType.BATCH)
                .setState(ApplicationState.NOT_STARTED)
                .setSubmitParams(batch)
                .setCreatedAt(LocalDateTime.now())
                .build();
        return applicationStorage.saveApplication(entity);
    }

    public Application update(Application application) {
        return applicationStorage.saveApplication(application);
    }

    public List<Application> fetchByState(ApplicationState state, Integer limit) {
        return applicationStorage.findApplicationsByStates(ApplicationType.BATCH, List.of(state), limit);
    }

    public List<Application> fetchRunning() {
        return applicationStorage
                .findApplicationsByStates(ApplicationType.BATCH, ApplicationState.runningStates(), Integer.MAX_VALUE);
    }

    public Optional<Application> fetchOne(String id) {
        return applicationStorage.findApplication(id);
    }

    public void deleteOne(String id) {
        backend.kill(id);
        applicationStorage.deleteApplication(id);
    }

}
