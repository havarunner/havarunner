package com.github.havarunner.annotation;

import com.github.havarunner.HavaRunnerSuite;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test as a part of a {@link HavaRunnerSuite}.
 *
 * @author Lauri Lehmijoki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PartOf {
    java.lang.Class<? extends HavaRunnerSuite<?>> value();
}
