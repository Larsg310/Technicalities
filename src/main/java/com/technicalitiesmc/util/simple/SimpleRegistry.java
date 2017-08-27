package com.technicalitiesmc.util.simple;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Marks a class for automatic creation of a registry.
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface SimpleRegistry {

    /**
     * The name of the registry.
     */
    String value();

    @SuppressWarnings("rawtypes")
    Class<? extends IForgeRegistryEntry> type() default IForgeRegistryEntry.class;

}
