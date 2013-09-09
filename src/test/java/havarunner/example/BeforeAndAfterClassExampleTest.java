package havarunner.example;

import com.google.common.collect.Sets;
import havarunner.HavaRunner;
import havarunner.TestWithMultipleScenarios;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(HavaRunner.class)
public class BeforeAndAfterClassExampleTest {

    static Universe universe;

    @BeforeClass
    static void create_the_universe() {
        universe = new Universe();
    }

    @AfterClass
    static void destroy_the_universe() {
        universe.destroy();
    }

    @Test
    void the_dude_above_created_the_universe_so_let_us_do_something_in_it() {
        assertNotNull(universe);
    }

    static class describe_befores_when_we_have_a_multiscenario_test implements TestWithMultipleScenarios<String> {

        static Grocery grocery;

        @BeforeClass
        static void setup_for_scenario() {
            grocery = new Grocery();
        }

        @Test
        void fruit_garden_contains_apples_and_oranges(String fruit) {
            assertTrue(grocery.containsFood(fruit));
        }

        @Override
        public Set<String> scenarios() {
            return Sets.newHashSet("pineapple", "banana");
        }

        static class Grocery {
            boolean containsFood(String fruit) {
                switch (fruit) {
                    case "pineapple":
                        return true;
                    case "banana":
                        return true;
                    default:
                        throw new IllegalArgumentException("The fruit garden does not contain the fruit " + fruit);
                }
            }
        }
    }

    static class Universe {
        boolean destroyed = false;

        void destroy() {
            destroyed = true;
        }
    }
}
