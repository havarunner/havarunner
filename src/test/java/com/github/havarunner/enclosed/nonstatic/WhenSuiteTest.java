package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.HavaRunner;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;

public class WhenSuiteTest {
    static volatile int outerFromSuiteCalled;
    static volatile int innerFromSuiteCalled;

    @Test
    public void HavaRunner_supports_non_static_classes_in_suite() {
        run(new HavaRunner(Suite.class));
        assertEquals(1, outerFromSuiteCalled);
        assertEquals(1, innerFromSuiteCalled);
    }
}
