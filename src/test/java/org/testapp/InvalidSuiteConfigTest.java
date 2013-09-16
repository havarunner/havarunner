package org.testapp;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.exception.SuiteMemberDoesNotBelongToSuitePackage;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.testapp.suite.TestAppSuite;

import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class InvalidSuiteConfigTest {

    @Test
    public void suite_members_must_be_within_the_same_package_as_the_suite() {
        Failure failure = runAndRecordFailure(new HavaRunner(InvalidSuiteMember.class));
        assertEquals(SuiteMemberDoesNotBelongToSuitePackage.class, failure.getException().getClass());
        assertEquals(
            "Suite member org.testapp.InvalidSuiteConfigTest$InvalidSuiteMember must be within the same package as the suite org.testapp.suite.TestAppSuite. Try moving InvalidSuiteMember under the package org.testapp.suite.",
            failure.getException().getMessage()
        );
    }

    @PartOf(TestAppSuite.class)
    static  class InvalidSuiteMember {

        private final String suiteObject;

        InvalidSuiteMember(String suiteObject) {

            this.suiteObject = suiteObject;
        }

        @Test
        public void test() {
            assertNotNull(suiteObject);
        }
    }

}
