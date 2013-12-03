package com.github.havarunner.beforeafter;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WhenBeforeFails {

    final List<Long> startedTests = Lists.newArrayList();
    final List<Long> finishedTests = Lists.newArrayList();
    final List<Long> failedTests = Lists.newArrayList();

    @Before
    public void runAndRecord() {
        new HavaRunner(BeforeFails.class).run(new RunNotifier() {
            @Override
            public void fireTestStarted(Description description) throws StoppedByUserException {
                doSleep();
                startedTests.add(System.currentTimeMillis());
            }

            @Override
            public void fireTestFailure(Failure failure) {
                doSleep();
                failedTests.add(System.currentTimeMillis());
            }

            @Override
            public void fireTestFinished(Description description) {
                doSleep();
                finishedTests.add(System.currentTimeMillis());
            }
        });
    }

    @Test
    public void HavaRunner_records_test_events_once_when_before_fails() {
        assertEquals(1, startedTests.size());
        assertEquals(1, failedTests.size());
        assertEquals(1, finishedTests.size());
    }

    @Test
    public void HavaRunner_records_test_events_in_correct_order_when_before_fails() {
        assertTrue(startedTests.get(0) < failedTests.get(0));
        assertTrue(failedTests.get(0) < finishedTests.get(0));
    }

    private void doSleep() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @RunSequentially
    static class BeforeFails {
        @Before
        public void fail() {
            throw new RuntimeException("Intentional");
        }

        @Test
        public void test() {

        }
    }
}
