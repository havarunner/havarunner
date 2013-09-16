package com.github.havarunner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a static method as a scenario provider.
 *
 * A scenario provider defines the set of scenario objects,
 * each of which will be passed to the test as a constructor argument.
 *
 * @author Lauri Lehmijoki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Scenarios {
}
