package com.github.havarunner.enclosed.nonstatic;

import org.junit.Test;

class SuperclassInNonStatic {

    @Test
    void outer() { }

    class Inner extends Superclass {

        @Test
        void test() { }
    }

    abstract static class Superclass { }
}
