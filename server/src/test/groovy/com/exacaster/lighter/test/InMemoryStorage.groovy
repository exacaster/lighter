package com.exacaster.lighter.test

import com.exacaster.lighter.application.Application
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import com.exacaster.lighter.log.Log
import com.exacaster.lighter.storage.ApplicationStorage
import com.exacaster.lighter.storage.Entity
import com.exacaster.lighter.storage.LogStorage
import com.exacaster.lighter.storage.SortOrder

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream

class InMemoryStorage implements ApplicationStorage, LogStorage {

    private final Map<Class<? extends Entity>, Map<String, Entity>> storage = new ConcurrentHashMap<>()

    @Override
    Optional<Application> findApplication(String internalApplicationId) {
        return findEntity(internalApplicationId, Application.class)
    }

    @Override
    List<Application> findApplications(ApplicationType type, Integer from, Integer size) {
        return findManyWithOffset({ type == it.getType() }, Application.class, from, size)
    }

    @Override
    void deleteApplication(String internalApplicationId) {
        deleteOne(internalApplicationId, Application.class)
        deleteOne(internalApplicationId, Log.class)
    }

    @Override
    Application saveApplication(Application application) {
        return storeEntity(application)
    }

    @Override
    List<Application> findApplicationsByStates(ApplicationType type, List<ApplicationState> states, SortOrder order, Integer offset, Integer limit) {
        return findMany({ type == it.getType() && states.contains(it.getState()) }, Application.class)
                .sorted((app1, app2) -> order == SortOrder.DESC ? app1.createdAt <=> app2.createdAt : app2.createdAt <=> app1.createdAt)
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList())
    }

    @Override
    Optional<Log> findApplicationLog(String internalApplicationId) {
        return findEntity(internalApplicationId, Log.class)
    }

    @Override
    Log saveApplicationLog(Log log) {
        return storeEntity(log)
    }

    void cleanup() {
        storage.clear()
    }

    private <T extends Entity> T storeEntity(T entity) {
        var entityStore = storage.computeIfAbsent(entity.getClass(), key -> new HashMap<>())
        entityStore.put(entity.getId(), entity)
        return entity
    }

    private <T extends Entity> Optional<T> findEntity(String id, Class<T> clazz) {
        var all = storage.get(clazz)
        if (all == null) {
            return Optional.empty()
        }
        return Optional.ofNullable(all.get(id) as T)
    }

    private <T extends Entity> List<T> findManyWithOffset(Predicate<T> filter, Class<T> clazz, Integer from, Integer size) {
        var all = storage.get(clazz)
        if (all == null) {
            return List.of()
        }

        return findMany(filter, clazz).skip(from).limit(size).collect(Collectors.toList())
    }

    private <T extends Entity> Stream<T> findMany(Predicate<T> filter, Class<T> clazz) {
        var all = storage.get(clazz)
        if (all == null) {
            return Stream.empty()
        }

        return all.values().stream()
                .sorted(Comparator.comparing(Entity::getCreatedAt).reversed())
                .map({ it as T })
                .filter(filter)
    }

    private <T extends Entity> void deleteOne(String id, Class<T> clazz) {
        var all = storage.get(clazz)
        if (all != null) {
            all.remove(id)
        }
    }
}
