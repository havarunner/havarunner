package havarunner;

import com.google.common.collect.Sets;
import havarunner.example.scenario.Person;
import havarunner.example.scenario.RestaurantMenuTest;
import havarunner.exception.ScenarioMethodNotFound;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class MultipleScenariosTest {

    @Test
    public void it_constructs_an_example_for_each_scenario() throws InitializationError {
        int numberOfScenarios = Person.values().length;
        assertEquals(
            numberOfScenarios * numberOfExamples(RestaurantMenuTest.class),
            new HavaRunner(RestaurantMenuTest.class).getChildren().size()
        );
    }

    @Test
    public void HavaRunner_raises_an_exception_if_the_scenario_method_does_not_have_the_scenario_object_as_only_arg() throws Exception {
        final AtomicReference<Failure> expectedFailure = runInvalidScenarioTestMethod();
        assertEquals(expectedFailure.get().getException().getClass(), ScenarioMethodNotFound.class);
    }

    @Test
    public void HavaRunner_prints_a_helpful_error_message_if_the_scenario_method_is_missing() throws Exception {
        final AtomicReference<Failure> expectedFailure = runInvalidScenarioTestMethod();
        assertEquals(
            expectedFailure.get().getMessage(),
            "Could not find the scenario method InvalidScenarioTest#this_method_is_missing_the_scenario_argument(java.lang.String). " +
                "Please add the method this_method_is_missing_the_scenario_argument(java.lang.String) into " +
                "class havarunner.MultipleScenariosTest$InvalidScenarioTest."
        );
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

    static class InvalidScenarioTest implements TestWithMultipleScenarios<String> {

        @Override
        public Set<String> scenarios() {
            return Sets.newHashSet("first", "second");
        }

        @Test
        void this_method_is_missing_the_scenario_argument() {

        }
    }
}
