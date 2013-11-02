package com.github.havarunner.suite;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.HavaRunnerSuite;
import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.annotation.Scenarios;
import com.github.havarunner.exception.ConstructorNotFound;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import java.util.List;

import static com.github.havarunner.TestHelper.run;
import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Enclosed.class)
public class RunningSuiteMembersIndividuallyTest {

    public static class when_one_suite_and_scenarios {
        static String suiteObject;
        static List<String> scenarios = Lists.newArrayList();

        @Test
        public void HavaRunner_passes_the_suite_and_scenario_objects_to_the_test_constructor() {
            run(new HavaRunner(test_with_scenarios_that_belongs_to_a_suite.class));
            assertEquals("this object is shared by all the tests in the suite", suiteObject);
            assertEquals(2, scenarios.size());
        }

        @PartOf(Suite.class)
        static class test_with_scenarios_that_belongs_to_a_suite {

            test_with_scenarios_that_belongs_to_a_suite(String suiteObj, String scenario) {
                suiteObject = suiteObj;
                scenarios.add(scenario);
            }

            @Test
            void test() {
            }

            @Scenarios
            static List<String> allScenarios() {
                return Lists.newArrayList("first", "second");
            }
        }
    }

    public static class when_one_suite_and_no_scenarios {
        static final List<String> receivedSuiteObjects = Lists.newArrayList();

        @Test
        public void HavaRunner_passes_the_suite_object_to_the_test_constructor() {
            run(new HavaRunner(TestInASuite.class));
            assertEquals("this object is shared by all the tests in the suite", receivedSuiteObjects.get(0));
        }

        @Test
        public void HavaRunner_creates_only_one_suite_instance_per_jvm() {
            run(new HavaRunner(TestInASuite.class));
            run(new HavaRunner(AnotherTestInTheSuite.class));
            assertTrue(receivedSuiteObjects.size() > 1);
            assertEquals(1, Sets.newHashSet(receivedSuiteObjects).size());
        }

        @PartOf(Suite.class)
        static class TestInASuite {

            TestInASuite(String suiteObj) {
                receivedSuiteObjects.add(suiteObj);
            }

            @Test
            void test() {
            }
        }

        @PartOf(Suite.class)
        static class AnotherTestInTheSuite {

            AnotherTestInTheSuite(String suiteObj) {
                receivedSuiteObjects.add(suiteObj);
            }

            @Test
            void test() {
            }
        }
    }

    public static class when_suite_member_is_missing_the_suite_object_constructor {

        @Test
        public void HavaRunner_gives_a_helpful_error_message_if_the_suite_test_does_not_have_the_required_constructor() {
            Failure failure = runAndRecordFailure(new HavaRunner(TestWithoutTheRequiredSuiteConstructor.class));
            assertEquals(ConstructorNotFound.class, failure.getException().getClass());
            assertEquals(
                "Class TestWithoutTheRequiredSuiteConstructor is missing the required constructor. Try adding the following constructor: com.github.havarunner.suite.RunningSuiteMembersIndividuallyTest$when_suite_member_is_missing_the_suite_object_constructor$TestWithoutTheRequiredSuiteConstructor.<init>(java.lang.String)",
                failure.getException().getMessage()
            );
        }


        @PartOf(Suite.class)
        static class TestWithoutTheRequiredSuiteConstructor {

            @Test
            void test() {
            }
        }
    }

    static class Suite implements HavaRunnerSuite<String> {
        public String suiteObject() {
            return "this object is shared by all the tests in the suite";
        }

        public void afterSuite() {
        }
    }
}
