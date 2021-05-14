package com.exacaster.lighter.storage;

import java.util.List;
import java.util.Optional;

public interface Storage {
    <T extends Entity> T storeEntity(T entity);
    <T extends Entity> Optional<T> findEntity(String id, Class<T> clazz);
    <T extends Entity> List<T> findMany(Integer from, Integer size, Class<T> clazz);
    <T extends Entity> void deleteOne(String id, Class<T> clazz);
}
