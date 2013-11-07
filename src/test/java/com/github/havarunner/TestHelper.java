package com.github.havarunner;

import com.github.havarunner.HavaRunner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHelper {
    public static Failure runAndRecordFailedAssumption(HavaRunner havaRunner) {
        final AtomicReference<Failure> expectedFailure = new AtomicReference<>();
        RunNotifier runNotifier = new RunNotifier() {
            @Override
            public void fireTestAssumptionFailed(Failure failure) {
                expectedFailure.set(failure);
            }
        };
        run(havaRunner, runNotifier);
        return expectedFailure.get();
    }

    public static Failure runAndRecordFailure(HavaRunner havaRunner) {
        final AtomicReference<Failure> expectedFailure = new AtomicReference<>();
        RunNotifier runNotifier = new RunNotifier() {
            @Override
            public void fireTestFailure(Failure failure) {
                expectedFailure.set(failure);
            }
        };
        run(havaRunner, runNotifier);
        return expectedFailure.get();
    }

    public static List<Failure> runAndRecordFailures(HavaRunner havaRunner) {
        final List<Failure> failures = Collections.synchronizedList(Lists.<Failure>newArrayList());
        RunNotifier runNotifier = new RunNotifier() {
            @Override
            public void fireTestFailure(Failure failure) {
                failures.add(failure);
            }
        };
        run(havaRunner, runNotifier);
        return failures;
    }

    public static void run(HavaRunner havaRunner, RunNotifier runNotifier) {
        havaRunner.run(runNotifier);
    }

    public static void runAndIgnoreErrors(HavaRunner havaRunner) {
        run(havaRunner, new RunNotifier());
    }

    public static void run(HavaRunner havaRunner) {
        run(havaRunner, new RunNotifier() {
            @Override
            public void fireTestFailure(Failure failure) {
                throw new RuntimeException(failure.getMessage());
            }
        });
    }

    public static void assertTestClasses(List<Class> expected, Class testClass) {
        Set<String> parsedClassNames = parsedTestClassNames(testClass);
        assertEquals(toName(expected), parsedClassNames);
    }

    private static Set<String> toName(List<Class> expected) {
        Set<String> classes = Sets.newTreeSet();
        for (Class aClass : expected) {
            classes.add(aClass.getName());
        }
        return classes;
    }

    private static Set<String> parsedTestClassNames(Class testClas) {
        Set<String> classes = Sets.newTreeSet();
        for (TestAndParameters testAndParameters : new HavaRunner(testClas).children()) {
            classes.add(testAndParameters.testClass().getName());
        }
        return classes;
    }

    public static void assertAllEqual(int expected, List<Integer> integers) {
        assertTrue(allEqual(expected, integers));
    }

    public static <T> boolean allEqual(final T expected, List<T> items) {
        return Iterables.all(items, new Predicate<T>() {
            public boolean apply(T input) {
                return input.equals(expected);
            }
        });
    }
}
