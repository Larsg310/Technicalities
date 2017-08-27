package com.technicalitiesmc.util;

import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.property.IUnlistedProperty;

/**
 * Generic implementation of {@link IUnlistedProperty} to avoid creating a subclass for each property.
 *
 * @see IUnlistedProperty
 */
public class UnlistedProperty<T> implements IUnlistedProperty<T> {

    private final String name;
    private final Class<T> clazz;
    private final Predicate<T> validate;
    private final Function<T, String> toString;

    protected UnlistedProperty(String name, Class<T> clazz, Predicate<T> validate, Function<T, String> toString) {
        this.name = name;
        this.clazz = clazz;
        this.validate = validate;
        this.toString = toString;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(T value) {
        return validate.test(value);
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }

    @Override
    public String valueToString(T value) {
        return toString.apply(value);
    }

    public static UnlistedProperty<ItemStack> ofStack(String name) {
        return ofStack(name, s -> true);
    }

    public static UnlistedProperty<ItemStack> ofStack(String name, Predicate<ItemStack> validate) {
        return new UnlistedProperty<>(name, ItemStack.class, validate, String::valueOf);
    }

    public static UnlistedProperty<Integer> ofInteger(String name) {
        return ofInteger(name, i -> true);
    }

    public static UnlistedProperty<Integer> ofInteger(String name, IntPredicate validate) {
        return new UnlistedProperty<>(name, Integer.class, validate::test, String::valueOf);
    }

    public static <T> UnlistedProperty<T> ofType(Class<T> type, String name) {
        return ofType(type, name, t -> true, String::valueOf);
    }

    public static <T> UnlistedProperty<T> ofType(Class<T> type, String name, Predicate<T> validate, Function<T, String> toString) {
        return new UnlistedProperty<T>(name, type, validate, toString);
    }

}
