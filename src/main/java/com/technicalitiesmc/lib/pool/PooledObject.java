package com.technicalitiesmc.lib.pool;

import com.google.common.base.Preconditions;

import java.util.UUID;
import java.util.function.Supplier;

public class PooledObject<T> implements Supplier<T> {

    private final DataPool<T> pool;
    private final UUID uuid;
    private T object;

    PooledObject(DataPool<T> pool, UUID uuid) {
        this.pool = pool;
        this.uuid = uuid;
    }

    public boolean isPresent() {
        return object != null;
    }

    @Override
    public T get() {
        Preconditions.checkState(object != null, "Attempted to getObject a pooled object that has not been requested.");
        return object;
    }

    public T getOrRequest() {
        if (!isPresent()) {
            request();
        }
        return get();
    }

    public UUID getUUID() {
        return uuid;
    }

    public void request() {
        object = pool.getObject(uuid);
        pool.request(uuid);
    }

    public void release() {
        object = null;
        pool.release(uuid);
    }
}
