package com.technicalitiesmc.util;

import com.google.common.base.Throwables;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * Represents a field in a class and allows the user to set it and get its value, no matter its visibility.
 */
public final class FieldPointer<P, T> {

    private final Class<P> parent;
    private final Class<T> type;
    private final String name;
    private final Field field;
    private final MethodHandle getter, setter;

    public FieldPointer(Class<P> parent, String... names) {
        this(ReflectionHelper.findField(parent, names));
    }

    @SuppressWarnings("unchecked")
    public FieldPointer(Field field) {
        this.parent = (Class<P>) field.getDeclaringClass();
        this.type = (Class<T>) field.getType();
        this.name = field.getName();
        this.field = field;

        try {
            getter = MethodHandles.lookup().unreflectGetter(field);
            setter = MethodHandles.lookup().unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    public Class<P> getParentType() {
        return parent;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T get(P parent) {
        try {
            return (T) getter.invoke(parent);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    public void set(P parent, T value) {
        try {
            setter.invoke(parent, value);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    public <A extends Annotation> A get(Class<A> annotation) {
        return field.getAnnotation(annotation);
    }

}
