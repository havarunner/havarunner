package com.github.havarunner.enclosed.nonstatic;

import org.junit.Test;

public class EnclosedInStaticInnerClass {

    static class StaticInner {
        @Test
        public void hello() { }

        class NonStaticInner {
            @Test
            public void hello() { }
        }
    }
}
