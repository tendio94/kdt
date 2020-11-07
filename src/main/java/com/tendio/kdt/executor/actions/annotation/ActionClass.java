package com.tendio.kdt.executor.actions.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface-marker for all classes that implement Actions.
 * Annotated classes will be found during initial actions registration lookup.
 * To be used in KdtApplication, every Actions class should either be annotated
 * or extend CommonActions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ActionClass {
    String description() default "";
}
