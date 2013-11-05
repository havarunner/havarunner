package com.github.havarunner;

import com.github.havarunner.annotation.RunSequentially;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class RunSequentiallyTest {

    public static class marking_tests_as_sequential {
        @Test
        public void HavaRunner_runs_tests_sequentially_when_the_RunSequentially_annotation_is_present_in_the_explicit_test_class() {
            assertTestsAreSequential(SequentialTest.class);
        }

        @Test
        public void HavaRunner_runs_tests_sequentially_when_the_RunSequentially_annotation_is_present_in_the_class_hierarchy() {
            assertTestsAreSequential(test_that_extends_a_sequential_test.class);
        }

        @Test
        public void HavaRunner_runs_tests_sequentially_when_the_RunSequentially_annotation_is_present_in_the_enclosing_class() {
            assertTestsAreSequential(SequentialEnclosingTest.class);
        }

        @Test
        public void two_sequential_tests_do_not_run_at_the_same_time() {

        }

        private void assertTestsAreSequential(Class testClass) {
            for (TestAndParameters testAndParameters : new HavaRunner(testClass).children()) {
                assertTrue(testAndParameters.runSequentially());
            }
        }

        @RunSequentially(because = "this test does not thrive in the concurrent world")
        static class SequentialTest {

            @Test
            void examples_in_this_class_are_run_sequentially() {
            }
        }

        static class test_that_extends_a_sequential_test extends SequentialTest {

            @Test
            void examples_in_this_class_are_run_sequentially() {
            }
        }

        @RunSequentially(because = "this test does not thrive in the concurrent world")
        static class SequentialEnclosingTest {

            static class EnclosedTest {
                @Test
                void examples_in_this_class_are_run_sequentially() {
                }
            }
        }
    }

    public static class running_two_sequential_tests {
        static final List<String> ints = Collections.synchronizedList(Lists.<String>newArrayList());

        @Test
        public void should_work_as_expected() {
            run(new HavaRunner(SequentialTest.class));
            if (ints.get(0).equals("from first")) { // The order in which the tests are run varies from JVM to JVM
                assertTrue(allEqual(ints.subList(0, 100), "from first"));
                assertTrue(allEqual(ints.subList(100, 200), "from second"));
            } else {
                assertTrue(allEqual(ints.subList(0, 100), "from second"));
                assertTrue(allEqual(ints.subList(100, 200), "from first"));
            }
        }

        private boolean allEqual(List<String> list, final String expected) {
            return Iterables.all(list, new Predicate<String>() {
                public boolean apply(String input) {
                    return input.equals(expected);
                }
            });
        }

        @RunSequentially(because = "this test does not thrive in the concurrent world")
        static class SequentialTest {

            @Test
            void first() throws InterruptedException {
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(1);
                    ints.add("from first");
                }
            }

            @Test
            void second() throws InterruptedException {
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(1);
                    ints.add("from second");
                }
            }
        }
    }
}
