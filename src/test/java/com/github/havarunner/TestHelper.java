package com.github.havarunner;

import com.github.havarunner.HavaRunner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.concurrent.atomic.AtomicReference;

public class TestHelper {
    public static AtomicReference<Throwable> runAndRecordFailedAssumption(HavaRunner havaRunner) {
        final AtomicReference<Throwable> expectedFailure = new AtomicReference<>();
        RunNotifier runNotifier = new RunNotifier() {
            @Override
            public void fireTestAssumptionFailed(Failure failure) {
                expectedFailure.set(failure.getException());
            }
        };
        run(havaRunner, runNotifier);
        return expectedFailure;
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

    public static void run(HavaRunner havaRunner) {
        run(havaRunner, new RunNotifier());
    }
}
