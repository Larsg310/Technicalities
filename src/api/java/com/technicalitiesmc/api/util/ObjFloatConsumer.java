package com.technicalitiesmc.api.util;

@FunctionalInterface
public interface ObjFloatConsumer<T> {

    public void accept(T obj, float val);

}
