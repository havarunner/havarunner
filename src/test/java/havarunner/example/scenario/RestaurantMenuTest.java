package havarunner.example.scenario;

import havarunner.HavaRunner;
import havarunner.scenario.TestWithMultipleScenarios;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(HavaRunner.class)
public class RestaurantMenuTest implements TestWithMultipleScenarios<Person> {

    @Test
    void it_is_cheaper_for_kids_than_adults() {
        RestaurantMenu menu = new RestaurantMenu(currentScenario());
        if (menu.person == Person.KID) {
            assertEquals(5, menu.price());
        } else {
            assertEquals(20, menu.price());
        }
    }

    @Test
    void it_contains_4_courses_for_kids_and_adults() {
        assertEquals(4, new RestaurantMenu(currentScenario()).courses());
    }

    @Test
    void for_adults_it_suggests_coffee() {
        if (currentScenario() == Person.KID) {
            assertEquals("ice cream", new RestaurantMenu(currentScenario()).suggestions());
        } else {
            assertEquals("coffee", new RestaurantMenu(currentScenario()).suggestions());
        }
    }

    @Override
    public Set<Person> scenarios() {
        Set<Person> persons = new HashSet<>();
        Collections.addAll(persons, Person.values());
        return persons;
    }

    @Override
    public Person currentScenario() {
        return null;
    }
}

