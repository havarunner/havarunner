package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.exception.NonStaticInnerClassMayNotHaveSuperclasses;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertEquals;

public class SuperclassesInNonStaticTest {

    @Test
    public void havarunner_should_reject_non_static_inner_classes_that_have_superclasses() {
        Failure failure = runAndRecordFailure(new HavaRunner(SuperclassInNonStatic.class));
        assertEquals(NonStaticInnerClassMayNotHaveSuperclasses.class, failure.getException().getClass());
        assertEquals(
            "The non-static inner class com.github.havarunner.enclosed.nonstatic.SuperclassInNonStatic$Inner should not have a superclass. " +
                "(HavaRunner considers deep class hiearchies as conceptual burden that hinder understandability.)",
            failure.getMessage()
        );
    }
}
