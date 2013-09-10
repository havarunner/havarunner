package havarunner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class BeforeClassTest {

    static boolean officeLightsAreOn = false;

    @Test
    public void HavaRunner_calls_the_BeforeClass_method_exactly_once() {
        run(new HavaRunner(FirstGuyInTheOffice.class));
        assertTrue(officeLightsAreOn);
    }

    static class FirstGuyInTheOffice {
        @BeforeClass
        static void turn_lights_on() {
            officeLightsAreOn = true;
        }

        @Test
        void do_office_work() {
            assertTrue(true);
        }
    }
}
