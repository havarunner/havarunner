package com.github.havarunner.scenarios;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.TestAndParameters;
import com.github.havarunner.annotation.Scenarios;
import com.github.havarunner.example.scenario.Person;
import com.github.havarunner.example.scenario.RestaurantMenuTest;
import com.github.havarunner.exception.ConstructorNotFound;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.havarunner.TestHelper.run;
import static com.github.havarunner.TestHelper.runAndRecordFailures;
import static org.junit.Assert.assertEquals;

public class MultipleScenariosTest {

    @Test
    public void it_constructs_an_example_for_each_scenario() throws InitializationError {
        int numberOfScenarios = Person.values().length;
        Iterable<TestAndParameters> children = new HavaRunner(RestaurantMenuTest.class).tests();
        assertEquals(
            numberOfScenarios * numberOfExamples(RestaurantMenuTest.class),
            Lists.newArrayList(children).size()
        );
    }

    @Test
    public void HavaRunner_raises_an_exception_if_the_scenario_method_does_not_have_the_scenario_object_as_only_arg() throws Exception {
        final AtomicReference<Failure> expectedFailure = runInvalidScenarioTestMethod();
        assertEquals(ConstructorNotFound.class, expectedFailure.get().getException().getClass());
    }

    @Test
    public void HavaRunner_prints_a_helpful_error_message_if_the_scenario_method_is_missing() throws Exception {
        final AtomicReference<Failure> expectedFailure = runInvalidScenarioTestMethod();
        assertEquals(
            "Class InvalidScenarioTest is missing the required constructor. Try adding the following constructor: InvalidScenarioTest(class java.lang.String)",
            expectedFailure.get().getMessage()
        );
    }

    final static Collection<String> scenarios = Lists.newArrayList();

    @Test
    public void HavaRunner_passes_each_scenario_object_to_the_scenario_method() throws Exception {
        run(new HavaRunner(ValidScenarioTest.class));
        assertEquals(
            Sets.newHashSet("first", "second"),
            Sets.newHashSet(scenarios)
        );
    }

    @Test
    public void HavaRunner_will_give_a_helpful_error_message_if_the_test_has_multiple_Scenario_annotations() {
        List<Failure> failures = runAndRecordFailures(new HavaRunner(TwoScenarioMethodsTest.class));
        assertEquals(
            "The class has two tests (one for each scenario). Both should fail because of the two @Scenario methods.",
            2,
            failures.size()
        );
        for (Failure failure : failures) {
            assertEquals(
                "Test com.github.havarunner.scenarios.MultipleScenariosTest$TwoScenarioMethodsTest has more than one @Scenario methods. Remove all but one of them.",
                failure.getMessage()
            );
        }
    }

    private AtomicReference<Failure> runInvalidScenarioTestMethod() throws InitializationError {
        final AtomicReference<Failure> expectedFailure = new AtomicReference<>();
        new HavaRunner(InvalidScenarioTest.class).run(new RunNotifier() {
            @Override
            public void fireTestFailure(Failure failure) {
                expectedFailure.set(failure);
            }
        });
        return expectedFailure;
    }

    int numberOfExamples(Class clazz) {
        int count = 0;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(Test.class) != null) {
                count += 1;
            }
        }
        return count;
    }

    static class InvalidScenarioTest {

        @Scenarios
        static Set<String> scenarios() {
            return Sets.newHashSet("foo", "bar");
        }

        @Test
        void this_test_is_missing_the_scenario_constructor() {

        }
    }

    static class TwoScenarioMethodsTest {
        final String scenario;

        TwoScenarioMethodsTest(String scenario) {
            this.scenario = scenario;
        }

        @Scenarios
        static Set<String> scenarios() {
            return Sets.newHashSet("foo", "bar");
        }

        @Scenarios
        static Set<String> scenarios2() {
            return Sets.newHashSet("foo", "bar");
        }

        @Test
        void test() {

        }
    }

    static class ValidScenarioTest {
        final String scenario;

        ValidScenarioTest(String scenario) {
            this.scenario = scenario;
        }

        @Scenarios
        static Set<String> scenarios() {
            return Sets.newHashSet("first", "second");
        }

        @Test
        void do_something_with_the_scenario() {
            scenarios.add(scenario);
        }

        @Test
        void do_something_else_with_the_scenario() {
            scenarios.add(scenario);
        }
    }

}
