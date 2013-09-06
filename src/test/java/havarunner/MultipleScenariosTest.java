package havarunner;

import havarunner.example.scenario.Person;
import havarunner.example.scenario.RestaurantMenuTest;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;

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

    int numberOfExamples(Class clazz) {
        int count = 0;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(Test.class) != null) {
                count += 1;
            }
        }
        return count;
    }
}
