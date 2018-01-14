package com.technicalitiesmc.lib.pool;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DataPool<T> {

    private final Map<UUID, T> objects = new HashMap<>();
    private final Map<UUID, AtomicInteger> usage = new HashMap<>();

    public UUID register(T object) {
        Preconditions.checkArgument(object != null, "Cannot register a null object to a data pool!");
        UUID uuid = UUID.randomUUID();
        objects.put(uuid, object);
        usage.put(uuid, new AtomicInteger(0));
        // TODO: Save world
        return uuid;
    }

    public void unregister(UUID uuid) {
        Preconditions.checkArgument(objects.remove(uuid) != null, "Invalid UUID!");
        usage.remove(uuid);
        // TODO: Save world
    }

    public PooledObject<T> getHandle(UUID uuid) {
        Preconditions.checkArgument(objects.containsKey(uuid), "Invalid UUID!");
        return new PooledObject<>(this, uuid);
    }

    public T getObject(UUID uuid) {
        T obj = objects.get(uuid);
        Preconditions.checkArgument(obj != null, "Invalid UUID!");
        return obj;
    }

    void request(UUID uuid) {
        AtomicInteger counter = usage.get(uuid);
        Preconditions.checkArgument(counter != null, "Invalid UUID!");
        counter.incrementAndGet();
    }

    void release(UUID uuid) {
        AtomicInteger counter = usage.get(uuid);
        Preconditions.checkArgument(counter != null, "Invalid UUID!");
        counter.decrementAndGet();
        // TODO: Potentially save to NBT
    }

    public Collection<T> getAll() {
        return objects.values();
    }
}
