package com.technicalitiesmc;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to mark Technicalities modules for load.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface TKModule {

    /**
     * Gets the name of the module.
     */
    String value();

    /**
     * Gets the modules this one depends on.<br/>
     * If any of them is not available, this one will not be loaded.<br/>
     * Also ensures this module loads after them.
     */
    String[] dependencies() default {};

    /**
     * Gets a set of modules this one should load after (apart from its dependencies).
     */
    String[] after() default {};

    /**
     * Gets whether this module should be on by default.
     */
    boolean enabledByDefault() default true;

    /**
     * Gets whether this module can be disabled.
     */
    boolean canBeDisabled() default true;

}
