package havarunner.example;

import com.google.common.collect.Sets;
import havarunner.HavaRunner;
import havarunner.scenario.TestWithMultipleScenarios;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(HavaRunner.class)
public class BeforeExampleTest {

    Object world;

    @Before
    void create_the_world() {
        world = new Object();
    }

    @Test
    void the_dude_above_created_the_world_so_let_us_do_something_in_it() {
        assertNotNull(world);
    }

    static class describe_befores_when_we_have_a_multiscenario_test implements TestWithMultipleScenarios<String> {

        FruitGarden fruitGarden;

        @Before
        void setup_for_scenario() {
            fruitGarden = new FruitGarden();
        }

        @Test
        void fruit_garden_contains_apples_and_oranges(String fruit) {
            assertTrue(fruitGarden.containsFruit(fruit));
        }

        @Override
        public Set<String> scenarios() {
            return Sets.newHashSet("apple", "orange");
        }

        static class FruitGarden {
            boolean containsFruit(String fruit) {
                switch (fruit) {
                    case "apple":
                        return true;
                    case "orange":
                        return true;
                    default:
                        throw new IllegalArgumentException("The fruit garden does not contain the fruit " + fruit);
                }
            }
        }
    }
}
