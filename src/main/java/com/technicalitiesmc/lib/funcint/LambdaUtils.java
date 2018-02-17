package com.technicalitiesmc.lib.funcint;

import com.google.common.base.Throwables;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Class with various utilities for use with lambdas.
 */
public class LambdaUtils {

    /**
     * Converts a {@link Callable} into a {@link Supplier}, re-throwing the exception if one happens.
     */
    public static <T> Supplier<T> safeSupplier(Callable<T> unsafe) {
        return () -> {
            try {
                return unsafe.call();
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        };
    }

    /**
     * Converts an {@link UnsafeConsumer} into a {@link Consumer}, re-throwing the exception if one happens.
     */
    public static <T> Consumer<T> safeConsumer(UnsafeConsumer<T> unsafe) {
        return (arg) -> {
            try {
                unsafe.accept(arg);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        };
    }

    /**
     * Converts an {@link UnsafeFunction} into a {@link Function}, re-throwing the exception if one happens.
     */
    public static <A, R> Function<A, R> safeFunction(UnsafeFunction<A, R> unsafe) {
        return (arg) -> {
            try {
                return unsafe.apply(arg);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        };
    }

    /**
     * Converts an {@link UnsafePredicate} into a {@link Predicate}, re-throwing the exception if one happens.
     */
    public static <T> Predicate<T> safePredicate(UnsafePredicate<T> unsafe) {
        return (arg) -> {
            try {
                return unsafe.test(arg);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        };
    }

    /**
     * Returns a function that casts given parameter of type T into type R if possible, otherwise returns null.
     * Useful for Optional.map operations
     */
    public static <T, R> Function<T, R> cast(Class<R> in) {
        return t -> {
            if (in.isInstance(t)) {
                // noinspection unchecked
                return (R) t;
            } else {
                return null;
            }
        };
    }

    /**
     * Dummy interface that represents a consumer which may throw an exception.
     */
    public static interface UnsafeConsumer<T> {

        public void accept(T arg) throws Exception;

    }

    /**
     * Dummy interface that represents a function which may throw an exception.
     */
    public static interface UnsafeFunction<A, R> {

        public R apply(A arg) throws Exception;

    }

    /**
     * Dummy interface that represents a predicate which may throw an exception.
     */
    public static interface UnsafePredicate<T> {

        public boolean test(T arg) throws Exception;

    }

}
