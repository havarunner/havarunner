package com.github.havarunner.timeout;

import com.github.havarunner.HavaRunner;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertTrue;

public class TimeoutTest {

    @Test
    public void HavaRunner_throws_an_exception_if_the_test_times_out() {
        Failure failure = runAndRecordFailure(new HavaRunner(TimedOut.class));
        assertTrue(failure.getMessage(), failure.getMessage().matches("Test timed out after \\d+ milliseconds"));
    }

    static class TimedOut {

        @Test(timeout = 10)
        public void longRunningOperation() throws InterruptedException {
            Thread.sleep(11);
        }
    }
}
