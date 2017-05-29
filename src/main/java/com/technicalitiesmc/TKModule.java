package com.technicalitiesmc;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface TKModule {

    String value();

    String[] dependencies() default {};

    String[] after() default {};

    boolean enabledByDefault() default true;

}
