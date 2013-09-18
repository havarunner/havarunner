package com.github.havarunner.enclosed;

import com.github.havarunner.HavaRunner;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class StaticNestedClassesTest {
    static boolean parentCalled;
    static boolean childCalled;
    static boolean grandchildCalled;
    static boolean grandGrandchildCalled;

    @Test
    public void HavaRunner_supports_static_inner_classes_recursively() {
        run(new HavaRunner(Parent.class));
        assertTrue(parentCalled);
        assertTrue(childCalled);
        assertTrue(grandchildCalled);
        assertTrue(grandGrandchildCalled);
    }

    static class Parent {
        @Test
        void test() {
            parentCalled = true;
        }
        static class Child {
            @Test
            void test() {
                childCalled = true;
            }
            static class Grandchild {
                @Test
                void test() {
                    grandchildCalled = true;
                }
                static class GrandGrandchild {
                    @Test
                    void test() {
                        grandGrandchildCalled = true;
                    }
                }
            }

        }
    }
}
