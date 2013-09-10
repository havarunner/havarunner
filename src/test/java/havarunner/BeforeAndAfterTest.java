package havarunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class BeforeAndAfterTest {

    static boolean worldIsBuilt = false;
    static boolean worldIsDestroyed = false;

    @Test
    public void HavaRunner_calls_the_Before_and_After_methods() {
        run(new HavaRunner(BeforeAndAfter.class));
        assertTrue(worldIsBuilt);
        assertTrue(worldIsDestroyed);
    }

    static class BeforeAndAfter {
        @Before
        void build_the_world() {
            worldIsBuilt = true;
        }

        @Test
        void live_in_the_world() {

        }

        @After
        void destroy_the_world() {
            worldIsDestroyed = true;
        }
    }
}
