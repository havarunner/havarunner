package com.github.havarunner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a zero-args method to be invoked after all the tests in a class.
 *
 * This annotation has similar sematincs to {@link org.junit.AfterClass}. The difference is that you can
 * use this annotation in an instance method, whereas the AfterClass method has to be attached to a static method.
 *
 * @author Lauri Lehmijoki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterAll {
}
