package com.technicalitiesmc.util.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class for automatic registration as a capability. This can be used to very easily register capabilities that don't have a default
 * implementation nor need to store any data.
 */
@Retention(RUNTIME)
@Target({TYPE, FIELD})
public @interface SimpleCapability {

}
