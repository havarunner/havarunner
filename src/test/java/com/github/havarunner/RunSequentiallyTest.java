package com.github.havarunner;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;

public class RunSequentiallyTest {

    static Thread sequentialTestThread;
    @Test
    public void HavaRunner_runs_tests_sequentially_when_the_RunSequentially_annotation_is_present_in_the_explicit_test_class() {
        run(new HavaRunner(SequentialTest.class));
        assertEquals(Thread.currentThread(), sequentialTestThread);
    }

    static Thread test_that_extends_a_sequential_test_Thread;
    @Test
    public void HavaRunner_runs_tests_sequentially_when_the_RunSequentially_annotation_is_present_in_the_class_hierarchy() {
        run(new HavaRunner(test_that_extends_a_sequential_test.class));
        assertEquals(Thread.currentThread(), test_that_extends_a_sequential_test_Thread);
    }

    static Thread sequentialEnclosingTestThread;
    @Test
    public void HavaRunner_runs_tests_sequentially_when_the_RunSequentially_annotation_is_present_in_the_enclosing_class() {
        run(new HavaRunner(SequentialEnclosingTest.class));
        assertEquals(Thread.currentThread(), sequentialEnclosingTestThread);
    }

    @RunSequentially
    static class SequentialTest {

        @Test
        void examples_in_this_class_are_run_sequentially() {
            sequentialTestThread = Thread.currentThread();
        }
    }

    static class test_that_extends_a_sequential_test extends SequentialTest {

        @Test
        void examples_in_this_class_are_run_sequentially() {
            test_that_extends_a_sequential_test_Thread = Thread.currentThread();
        }
    }

    @RunSequentially
    static class SequentialEnclosingTest {

        @Test
        void examples_in_this_class_are_run_sequentially() {
            sequentialEnclosingTestThread = Thread.currentThread();
        }
    }
}
