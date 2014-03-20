package com.github.havarunner.suite.covariant;
import com.github.havarunner.HavaRunner;
import org.junit.After;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;


public class SuiteConstructorSubclassTest {

    static boolean suiteMemberTestInvoked = false;

    @After
    public void reset() {
        suiteMemberTestInvoked = false;
    }

    @Test
    public void havarunner_should_support_covariant_suite_objects(){
        run(new HavaRunner(Suite.class));
        assertTrue(suiteMemberTestInvoked);
    }
}
