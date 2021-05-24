package com.exacaster.lighter.batch;

import com.exacaster.lighter.storage.Storage;
import java.util.List;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class BatchService {

    private final Storage storage;
    private final List<OnBatchCreate> createObservers;

    public BatchService(Storage storage, List<OnBatchCreate> createObservers) {
        this.storage = storage;
        this.createObservers = createObservers;
    }

    public List<Batch> fetch(Integer from, Integer size) {
        return storage.findMany(from, size, BatchData.class).stream()
                .map(this::toBatch)
                .toList();
    }

    public Batch create(BatchConfiguration batch) {
        var entity = new BatchData(UUID.randomUUID().toString(), null, "", BatchState.not_started, batch);
        var created = toBatch(storage.storeEntity(entity));
        createObservers.forEach(ob -> ob.onBatchCreate(created));
        return created;
    }

    public Batch fetchOne(String id) {
        return storage.findEntity(id, BatchData.class)
                .map(this::toBatch)
                .orElse(null);
    }

    public void deleteOne(String id) {
        storage.deleteOne(id, BatchData.class);
    }

    private Batch toBatch(BatchData data) {
        return new Batch(data.id(), data.appId(), data.appInfo(), data.state());
    }
}
