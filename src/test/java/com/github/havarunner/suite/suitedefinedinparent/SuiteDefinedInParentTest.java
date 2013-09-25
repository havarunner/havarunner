package com.github.havarunner.suite.suitedefinedinparent;

import com.github.havarunner.HavaRunner;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class SuiteDefinedInParentTest {

    static boolean suiteMethodCalled;

    @Test
    public void abstract_classes_may_declare_suite_membership() {
        run(new HavaRunner(SuiteMember.class));
        assertTrue(suiteMethodCalled);
    }
}
