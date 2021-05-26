package com.exacaster.lighter.batch;

import com.exacaster.lighter.backend.Application;
import com.exacaster.lighter.backend.ApplicationState;
import com.exacaster.lighter.backend.ApplicationType;
import com.exacaster.lighter.spark.SubmitParams;
import com.exacaster.lighter.storage.Storage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class BatchService {

    private final Storage storage;

    public BatchService(Storage storage) {
        this.storage = storage;
    }

    public List<Application> fetch(Integer from, Integer size) {
        return storage.findApplications(ApplicationType.BATCH, from, size);
    }

    public Application create(SubmitParams batch) {
        var entity = new Application(UUID.randomUUID().toString(), ApplicationType.BATCH, ApplicationState.NOT_STARTED, "", "", batch);
        return storage.saveApplication(entity);
    }

    public Application update(Application application) {
        return storage.saveApplication(application);
    }

    public List<Application> fetchByState(ApplicationState state) {
        return storage.findApplicationsByStates(ApplicationType.BATCH, List.of(state));
    }

    public List<Application> fetchNonFinished() {
        return storage.findApplicationsByStates(ApplicationType.BATCH, ApplicationState.incompleteStates());
    }

    public Optional<Application> fetchOne(String id) {
        return storage.findApplication(id);
    }

    public void deleteOne(String id) {
        storage.deleteApplication(id);
    }

}
