package com.github.havarunner;

import com.github.havarunner.annotation.AfterAll;
import org.junit.Test;
import org.junit.runner.Description;

import static com.github.havarunner.TestHelper.runAndRecordIgnored;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class AssumeThatTest {

   @Test
   public void HavaRunner_ignores_tests_that_use_the_assume_API_of_JUnit() {
       Description description = runAndRecordIgnored(new HavaRunner(AssumptionDoesNotHold.class));
       assertEquals("skipMe", description.getMethodName());
   }

    @Test
    public void the_assume_API_call_may_be_in_the_constructor() {
        Description description = runAndRecordIgnored(new HavaRunner(AssumptionDoesNotHoldInConstructor.class));
        assertEquals("skipMe", description.getMethodName());
    }

   static class AssumptionDoesNotHold {

       @Test
       public void skipMe() {
           assumeTrue(false);
       }
   }

    static class AssumptionDoesNotHoldInConstructor {
        AssumptionDoesNotHoldInConstructor() {
            assumeTrue(false);
        }

        @Test
        public void skipMe() {

        }

        @AfterAll
        public void cleanup() {

        }
    }
}
