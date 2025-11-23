package com.exacaster.lighter.application.batch;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.application.SubmitParams;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.storage.ApplicationStorage;
import com.exacaster.lighter.storage.SortOrder;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class BatchService {

    private final ApplicationStorage applicationStorage;
    private final Backend backend;

    public BatchService(ApplicationStorage applicationStorage, Backend backend) {
        this.applicationStorage = applicationStorage;
        this.backend = backend;
    }

    public List<Application> fetch(Integer from, Integer size) {
        return applicationStorage.findApplications(EnumSet.of(ApplicationType.BATCH), from, size);
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

    public List<Application> fetchByState(ApplicationState state, SortOrder order, Integer from, Integer limit) {
        return applicationStorage.findApplicationsByStates(ApplicationType.BATCH, List.of(state), order, from, limit);
    }

    public List<Application> fetchRunning() {
        return applicationStorage
                .findApplicationsByStates(ApplicationType.BATCH, ApplicationState.runningStates(), SortOrder.ASC, 0, Integer.MAX_VALUE);
    }

    public Optional<Application> fetchOne(String id) {
        return applicationStorage.findApplication(id);
    }

    public void deleteOne(String id) {
        this.fetchOne(id).ifPresent(app -> {
            backend.kill(app);
            applicationStorage.deleteApplication(id);
        });
    }

    public List<Application> fetchFinishedBatchesOlderThan(LocalDateTime cutoffDate) {
        return applicationStorage.findApplicationsByStates(
                ApplicationType.BATCH,
                ApplicationState.finishedStates(),
                SortOrder.ASC,
                0,
                100
        ).stream()
                .filter(app -> app.getContactedAt() != null && app.getContactedAt().isBefore(cutoffDate))
                .collect(Collectors.toList());
    }

}
