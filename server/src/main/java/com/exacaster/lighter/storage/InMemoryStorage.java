package com.exacaster.lighter.storage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage implements Storage {

    private final Map<Class, Map<String, Object>> storage = new ConcurrentHashMap<>();

    @Override
    public <T extends Entity> T storeEntity(T entity) {
        var entityStore = storage.computeIfAbsent(entity.getClass(), key -> new ConcurrentHashMap<>());
        entityStore.put(entity.id(), entity);
        return entity;
    }

    @Override
    public <T extends Entity> Optional<T> findEntity(String id, Class<T> clazz) {
        var all = storage.get(clazz);
        if (all == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) all.get(id));
    }

    @Override
    public <T extends Entity> List<T> findMany(Integer from, Integer size, Class<T> clazz) {
        var all = storage.get(clazz);
        if (all == null) {
            return List.of();
        }

        return all.values().stream().map(clazz::cast).skip(from).limit(size).toList();
    }

    @Override
    public <T extends Entity> void deleteOne(String id, Class<T> clazz) {
        var all = storage.get(clazz);
        if (all != null) {
            all.remove(id);
        }
    }
}
