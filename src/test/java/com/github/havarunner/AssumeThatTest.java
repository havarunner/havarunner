package com.github.havarunner;

import org.junit.Test;
import org.junit.runner.Description;

import static com.github.havarunner.TestHelper.runAndRecordIgnored;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class AssumeThatTest {

   @Test
   public void HavaRunner_ignores_tests_that_use_the_assume_api_of_JUnit() {
       Description description = runAndRecordIgnored(new HavaRunner(AssumptionDoesNotHold.class));
       assertEquals("skipMe", description.getMethodName());
   }

   static class AssumptionDoesNotHold {

       @Test
       public void skipMe() {
           assumeTrue(false);
       }
   }
}
