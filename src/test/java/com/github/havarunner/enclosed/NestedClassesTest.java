package com.github.havarunner.enclosed;

import com.github.havarunner.HavaRunner;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class NestedClassesTest {
    static boolean parentCalled;
    static boolean childCalled;
    static boolean grandchildCalled;
    static boolean grandGrandchildCalled;

    @Test
    public void HavaRunner_supports_nested_classes_recursively() {
        run(new HavaRunner(Parent.class));
        assertTrue("parent must be called", parentCalled);
        assertTrue("child must be called", childCalled);
        assertTrue("grandchild must be called", grandchildCalled);
        assertTrue("grandgrandchild must be called", grandGrandchildCalled);
    }

    static class Parent {
        @Test
        void test() {
            parentCalled = true;
        }
        class Child {
            @Test
            void test() {
                childCalled = true;
            }
            class Grandchild {
                @Test
                void test() {
                    grandchildCalled = true;
                }
                class GrandGrandchild {
                    @Test
                    void test() {
                        grandGrandchildCalled = true;
                    }
                }
            }

        }
    }
}
