package com.github.havarunner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * With this annotation, you can instruct HavaRunner to run tests of a class sequentially.
 *
 * If you mark a {@link com.github.havarunner.HavaRunnerSuite} with this annotation, HavaRunner will
 * run sequentially all the tests of the suite. If a member of a sequentially-run suite contains this annotation, it
 * will override the sequentiality specification of the suite.
 *
 * To speed up sequential tests, you can specify a sequentiality context with
 * {@link com.github.havarunner.annotation.RunSequentially#with()}.
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
    SequentialityContext with() default SequentialityContext.JEAN_LUC_PICARD;

    public static enum SequentialityContext {
        JEAN_LUC_PICARD,
        TESTS_OF_SAME_INSTANCE
    }
}
