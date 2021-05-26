package com.exacaster.lighter.storage;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.log.Log;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InMemoryStorage implements Storage {

    private final Map<Class<? extends Entity>, Map<String, Object>> storage = new ConcurrentHashMap<>();


    @Override
    public Optional<Application> findApplication(String internalApplicationId) {
        return findEntity(internalApplicationId, Application.class);
    }

    @Override
    public List<Application> findApplications(ApplicationType type, Integer from, Integer size) {
        return findMany(job -> type.equals(job.type()), Application.class, from, size);
    }

    @Override
    public void deleteApplication(String internalApplicationId) {
        deleteOne(internalApplicationId, Application.class);
        deleteOne(internalApplicationId, Log.class);
    }

    @Override
    public Application saveApplication(Application application) {
        return storeEntity(application);
    }

    @Override
    public List<Application> findApplicationsByStates(ApplicationType type, List<ApplicationState> states) {
        return  findMany(job -> type.equals(job.type()) && states.contains(job.state()), Application.class).toList();
    }

    @Override
    public Optional<Log> findApplicationLog(String internalApplicationId) {
        return findEntity(internalApplicationId, Log.class);
    }

    @Override
    public Log saveApplicationLog(Log log) {
        return storeEntity(log);
    }

    public <T extends Entity> T storeEntity(T entity) {
        var entityStore = storage.computeIfAbsent(entity.getClass(), key -> new ConcurrentHashMap<>());
        entityStore.put(entity.id(), entity);
        return entity;
    }

    private <T extends Entity> Optional<T> findEntity(String id, Class<T> clazz) {
        var all = storage.get(clazz);
        if (all == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) all.get(id));
    }

    private <T extends Entity> List<T> findMany(Predicate<T> filter, Class<T> clazz, Integer from, Integer size) {
        var all = storage.get(clazz);
        if (all == null) {
            return List.of();
        }

        return findMany(filter, clazz).skip(from).limit(size).toList();
    }

    private <T extends Entity> Stream<T> findMany(Predicate<T> filter, Class<T> clazz) {
        var all = storage.get(clazz);
        if (all == null) {
            return Stream.empty();
        }

        return all.values().stream().map(clazz::cast).filter(filter);
    }

    private <T extends Entity> void deleteOne(String id, Class<T> clazz) {
        var all = storage.get(clazz);
        if (all != null) {
            all.remove(id);
        }
    }
}
