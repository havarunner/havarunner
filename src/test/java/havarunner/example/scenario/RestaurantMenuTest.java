package havarunner.example.scenario;

import havarunner.HavaRunner;
import havarunner.TestWithMultipleScenarios;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(HavaRunner.class)
public class RestaurantMenuTest implements TestWithMultipleScenarios<Person> {

    @Test
    void it_is_cheaper_for_kids_than_adults(Person person) {
        RestaurantMenu menu = new RestaurantMenu(person);
        if (menu.person.equals(Person.KID)) {
            assertEquals(5, menu.price());
        } else {
            assertEquals(20, menu.price());
        }
    }

    @Test
    void it_contains_4_courses_for_kids_and_adults(Person person) {
        assertEquals(4, new RestaurantMenu(person).courses());
    }

    @Test
    void for_adults_it_suggests_coffee(Person person) {
        if (person == Person.KID) {
            assertEquals("ice cream", new RestaurantMenu(person).suggestions());
        } else {
            assertEquals("coffee", new RestaurantMenu(person).suggestions());
        }
    }

    @Override
    public Set<Person> scenarios() {
        Set<Person> persons = new HashSet<>();
        Collections.addAll(persons, Person.values());
        return persons;
    }
}

