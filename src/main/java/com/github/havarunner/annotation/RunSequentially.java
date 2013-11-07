package com.github.havarunner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Forces the test to be run in sequence with other tests that are marked with this annotation.
 *
 * @author Lauri Lehmijoki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RunSequentially {

    /**
     * @return the reason why this test is run sequentially (for documentation purposes)
     */
    String because();

    /**
     * @return the type of test with witch to synchronise
     */
    SequentialityContext with() default SequentialityContext.TESTS_OF_SAME_INSTANCE;

    public static enum SequentialityContext {
        JEAN_LUC_PICARD,
        TESTS_OF_SAME_INSTANCE
    }
}
