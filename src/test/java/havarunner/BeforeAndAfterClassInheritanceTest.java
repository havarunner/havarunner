package havarunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static havarunner.TestHelper.run;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BeforeAndAfterClassInheritanceTest {

    static Long grandParentBeforeClassCalled;
    static Long grandParentAfterClassCalled;
    static Long parentBeforeClassCalled;
    static Long parentAfterClassCalled;
    static Long childBeforeClassCalled;
    static Long childAfterClassCalled;

    @Test
    public void HavaRunner_calls_the_AfterClass_and_BeforeClass_methods_in_correct_order() {
        run(new HavaRunner(Child.class));
        TreeSet<Long> callDates = Sets.newTreeSet(Sets.newHashSet(
            checkNotNull(grandParentAfterClassCalled),
            checkNotNull(grandParentBeforeClassCalled),
            checkNotNull(parentAfterClassCalled),
            checkNotNull(parentBeforeClassCalled),
            checkNotNull(childAfterClassCalled),
            checkNotNull(childBeforeClassCalled)
        ));
        assertEquals(
            Lists.newArrayList(callDates),
            Lists.newArrayList(
                grandParentBeforeClassCalled,
                parentBeforeClassCalled,
                childBeforeClassCalled,
                grandParentAfterClassCalled,
                parentAfterClassCalled,
                childAfterClassCalled
            )
        );
    }

    static class GrandParent {
        @BeforeClass
        static void beforeClass() throws InterruptedException {
            grandParentBeforeClassCalled = System.currentTimeMillis();
            Thread.sleep(1);
        }

        @AfterClass
        static void afterClass() throws InterruptedException {
            grandParentAfterClassCalled = System.currentTimeMillis();
            Thread.sleep(1);
        }

        @Test
        void hello_word() {
            assertTrue(true);
        }
    }

    static class Parent extends GrandParent {
        @BeforeClass
        static void beforeClass() throws InterruptedException {
            parentBeforeClassCalled = System.currentTimeMillis();
            Thread.sleep(1);
        }

        @AfterClass
        static void afterClass() throws InterruptedException {
            parentAfterClassCalled = System.currentTimeMillis();
            Thread.sleep(1);
        }

        @Test
        void hello_word() {
            assertTrue(true);
        }
    }

    static class Child extends Parent {
        @BeforeClass
        static void beforeClass() throws InterruptedException {
            childBeforeClassCalled = System.currentTimeMillis();
            Thread.sleep(1);
        }

        @AfterClass
        static void afterClass() throws InterruptedException {
            childAfterClassCalled = System.currentTimeMillis();
            Thread.sleep(1);
        }

        @Test
        void hello_word() {
            assertTrue(true);
        }
    }
}
