package com.github.havarunner.sequentiality;

import com.github.havarunner.ConcurrencyControl;
import com.github.havarunner.HavaRunner;
import com.github.havarunner.TestAndParameters;
import com.github.havarunner.TestHelper;
import com.github.havarunner.annotation.RunSequentially;
import com.github.havarunner.annotation.Scenarios;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static com.github.havarunner.ConcurrencyControl.semaphore;
import static com.github.havarunner.TestHelper.addHundredTimes;
import static com.github.havarunner.TestHelper.assertAllEqual;
import static com.github.havarunner.TestHelper.run;
import static com.google.common.collect.Collections2.filter;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class RunSequentiallyTestsOfSameInstance {

    public static class when_same_class_but_different_scenario {
        private final List<TestAndParameters> children = Lists.newArrayList(new HavaRunner(Test1.class).children());

        @Test
        public void HavaRunner_will_run_them_in_parallel() {
            boolean testsUseSameSemaphore =
                semaphore(filterByScenario("first", children).get(0))
                .equals(semaphore(filterByScenario("second", children).get(1)));
            assertFalse(testsUseSameSemaphore);
        }

        private List<TestAndParameters> filterByScenario(final String scenario, List<TestAndParameters> children) {
            return Lists.newArrayList(Collections2.filter(children, new Predicate<TestAndParameters>() {
                @Override
                public boolean apply(@Nullable TestAndParameters testAndParameters) {
                    return testAndParameters.scenario().get().equals(scenario);
                }
            }));
        }

        @RunSequentially(
            because = "this test doesn't thrive in the concurrent world",
            with = RunSequentially.SequentialityContext.TESTS_OF_SAME_INSTANCE
        )
        static class Test1 {

            Test1(String scenarioObject) {}

            @Test
            void test() throws InterruptedException {
            }

            @Test
            void another() throws InterruptedException {
            }

            @Scenarios
            static Collection<String> scenarios() {
                return Lists.newArrayList("first", "second");
            }
        }
    }

    public static class when_there_are_two_tests {

        private final List<TestAndParameters> children = Lists.newArrayList(new HavaRunner(Enclosing.class).children());

        @Test
        public void HavaRunner_assigns_each_test_a_semaphore_of_size_1() {
            for (TestAndParameters child : children) {
                assertEquals(1, ConcurrencyControl.semaphore(child).availablePermits());
            }
        }

        @Test
        public void HavaRunner_assigns_each_test_a_unique_semaphore() {
            List<TestAndParameters> ofTest1 = filterByClass(Enclosing.Test1.class);
            List<TestAndParameters> ofTest2 = filterByClass(Enclosing.Test2.class);
            assertTrue(semaphore(ofTest1.get(0)) != semaphore(ofTest2.get(0)));
        }

        @Test
        public void HavaRunner_assigns_tests_of_same_instance_the_same_semaphore() {
            List<TestAndParameters> ofTest1 = filterByClass(Enclosing.Test1.class);
            assertTrue(semaphore(ofTest1.get(0)) == semaphore(ofTest1.get(1)));
        }

        private List<TestAndParameters> filterByClass(final Class clazz) {
            return Lists.newArrayList(filter(children, new Predicate<TestAndParameters>() {
                @Override
                public boolean apply(@Nullable TestAndParameters input) {
                    return input.testClass().equals(clazz);
                }
            }));
        }

        static class Enclosing {
            @RunSequentially(
                because = "this test doesn't thrive in the concurrent world",
                with = RunSequentially.SequentialityContext.TESTS_OF_SAME_INSTANCE
            )
            static class Test1 {
                @Test
                void test() {
                }

                @Test
                void another() {
                }
            }

            @RunSequentially(
                because = "this test doesn't thrive in the concurrent world",
                with = RunSequentially.SequentialityContext.TESTS_OF_SAME_INSTANCE
            )
            static class Test2 {
                @Test
                void test() {
                }
            }
        }
    }

    public static class when_suite_is_marked_as_RunSequentially {
        @Test
        public void HavaRunner_marks_the_test_to_be_RunSequentially() {
            TestAndParameters test = findByClass(new HavaRunner(SequentialSuite.class).children(), SuiteMember.class);
            assertTrue(test.runSequentially().isDefined());
            assertEquals("tests in this suite do not thrive in the concurrent world", test.runSequentially().get().because());
        }

        @Test
        public void HavaRunner_lets_the_test_override_the_RunSequentially_spec_of_the_suite() {
            TestAndParameters test = findByClass(
                new HavaRunner(SequentialSuite.class).children(),
                SuiteMemberOverridingRunSequentially.class
            );
            assertTrue(test.runSequentially().isDefined());
            assertEquals("this suite member has its own reason for sequentiality", test.runSequentially().get().because());
        }

        private TestAndParameters findByClass(Iterable<TestAndParameters> children, final Class clazz) {
            return Iterables.find(children, new Predicate<TestAndParameters>() {
                public boolean apply(TestAndParameters input) {
                    return input.testClass().equals(clazz);
                }
            });
        }
    }

    public static class when_there_is_only_one_test {
        static List<Integer> items = Lists.newArrayList();

        @BeforeClass
        public static void runTest() {
            run(new HavaRunner(SeqTest.class));
        }

        @Test
        public void HavaRunner_can_run_sequentially_only_tests_of_same_instance() {
            if (items.get(0).equals(1)) { // if-else, because we can't be sure of the order in which the test methods are run
                assertAllEqual(1, items.subList(0, 100));
                assertAllEqual(2, items.subList(100, 200));
            } else {
                assertAllEqual(2, items.subList(0, 100));
                assertAllEqual(1, items.subList(100, 200));
            }
        }

        @RunSequentially(
            because = "this test doesn't thrive in the concurrent world",
            with = RunSequentially.SequentialityContext.TESTS_OF_SAME_INSTANCE
        )
        static class SeqTest {
            @Test
            void test1() throws InterruptedException {
                addHundredTimes(1, items);
            }

            @Test
            void test2() throws InterruptedException {
                addHundredTimes(2, items);
            }
        }
    }

}
