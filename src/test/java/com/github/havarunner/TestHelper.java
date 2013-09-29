package com.github.havarunner;

import com.github.havarunner.HavaRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.concurrent.atomic.AtomicReference;

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
}
