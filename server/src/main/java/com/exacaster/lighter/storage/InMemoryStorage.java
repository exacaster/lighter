package com.exacaster.lighter.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage implements Storage {

    private final Map<Class, Map<Long, Object>> storage = new ConcurrentHashMap<>();

    @Override
    public <T extends Entity> T storeEntity(T entity) {
        var entityStore = storage.computeIfAbsent(entity.getClass(), key -> new ConcurrentHashMap<>());
        entityStore.put(entity.id(), entity);
        return entity;
    }

    public <T extends Entity> T findEntity(Long id, Class<T> clazz) {
        return (T) storage.get(clazz).get(id);
    }
}
