package havarunner;

import org.junit.AfterClass;
import org.junit.Test;

import static havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class AfterClassTest {

    static boolean officeLightsAreOff = false;

    @Test
    public void HavaRunner_calls_the_AfterClass_method_exactly_once() {
        run(new HavaRunner(LastGuyInTheOffice.class));
        assertTrue(officeLightsAreOff);
    }

    static class LastGuyInTheOffice {
        @Test
        void do_office_work() {
            assertTrue(true);
        }

        @AfterClass
        static void shut_down_lights() {
            officeLightsAreOff = true;
        }
    }
}
