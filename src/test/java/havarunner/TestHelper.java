package havarunner;

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

    public static void run(HavaRunner havaRunner, RunNotifier runNotifier) {
        havaRunner.run(runNotifier);
    }

    public static void run(HavaRunner havaRunner) {
        run(havaRunner, new RunNotifier());
    }
}
