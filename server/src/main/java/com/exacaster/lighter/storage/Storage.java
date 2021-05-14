package com.exacaster.lighter.storage;

public interface Storage {
    <T extends Entity> T storeEntity(T entity);
}
