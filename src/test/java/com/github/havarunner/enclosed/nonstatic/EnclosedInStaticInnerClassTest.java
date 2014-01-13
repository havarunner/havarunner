package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.TestAndParameters;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.junit.Assert.assertTrue;

public class EnclosedInStaticInnerClassTest {

    @Test
    public void havarunner_should_support_non_static_inner_classes_in_static_nested_classes() {
        List<Class<?>> tests = transform(newArrayList(new HavaRunner(EnclosedInStaticInnerClass.StaticInner.class).tests()), toClass);
        assertTrue(tests.contains(EnclosedInStaticInnerClass.StaticInner.class));
        assertTrue(tests.contains(EnclosedInStaticInnerClass.StaticInner.NonStaticInner.class));
    }

    Function<TestAndParameters,Class<?>> toClass = new Function<TestAndParameters, Class<?>>() {
        public Class<?> apply(TestAndParameters input) {
            return input.testClass();
        }
    };
}
