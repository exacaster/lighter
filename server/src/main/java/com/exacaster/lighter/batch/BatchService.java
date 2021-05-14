package com.exacaster.lighter.batch;

import com.exacaster.lighter.storage.Storage;
import java.util.List;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class BatchService {

    private final Storage storage;

    public BatchService(Storage storage) {
        this.storage = storage;
    }

    public List<Batch> fetch(Integer from, Integer size) {
        return storage.findMany(from, size, BatchData.class).stream()
                .map(this::toBatch)
                .toList();
    }

    public Batch create(BatchConfiguration batch) {
        var entity = new BatchData(UUID.randomUUID().toString(), null, "", BatchState.not_started, batch);
        return toBatch(storage.storeEntity(entity));
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
