package com.technicalitiesmc.util.simple;

import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class for automatic creation of a registry.
 */
@Retention(RUNTIME)
@Target({TYPE, FIELD})
public @interface SimpleRegistry {

    /**
     * The name of the registry.
     */
    String value();

    @SuppressWarnings("rawtypes")
    Class<? extends IForgeRegistryEntry> type() default IForgeRegistryEntry.class;

}
