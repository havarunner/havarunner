package com.github.havarunner;

import com.github.havarunner.annotation.AfterAll;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.havarunner.TestHelper.run;
import static com.github.havarunner.TestHelper.runAndIgnoreErrors;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class AfterTest {

    public static class when_the_test_class_contains_multiple_tests {
        static Long test1MethodCall;
        static Long test2MethodCall;
        static List<Long> afterMethodCalls = new CopyOnWriteArrayList<>();

        @Test
        public void HavaRunner_calls_the_After_method_once_for_the_instance() {
            run(new HavaRunner(MultipleTests.class));
            assertEquals(1, afterMethodCalls.size());
        }

        static class MultipleTests {
            @Test
            void test_1() {
                assertNull(test1MethodCall);
                test1MethodCall = System.currentTimeMillis();
            }

            @Test
            void test_2() {
                assertNull(test2MethodCall);
                test2MethodCall = System.currentTimeMillis();
            }

            @AfterAll
            void cleanUp() throws InterruptedException {
                Thread.sleep(1); // Sleep. Otherwise millisecond-precision is not enough.
                afterMethodCalls.add(System.currentTimeMillis());
            }
        }
    }

    public static class when_test__fails {
        static boolean worldIsBuilt = false;
        static boolean worldIsDestroyed = false;

        @Test
        public void HavaRunner_calls_the_After_method_even_if_the_test_throws_an_exception() {
            runAndIgnoreErrors(new HavaRunner(AfterFailingTest.class));
            assertTrue(worldIsBuilt);
            assertTrue(worldIsDestroyed);
        }

        static class AfterFailingTest {
            AfterFailingTest() {
                worldIsBuilt = true;
            }

            @Test
            void live_in_the_world() {
                fail("This test always fails");
            }

            @AfterAll
            void destroy_the_world() {
                worldIsDestroyed = true;
            }
        }
    }

    public static class when_test_passes {
        static boolean worldIsBuilt = false;
        static boolean worldIsDestroyed = false;

        @Test
        public void HavaRunner_calls_the_After_method() {
            run(new HavaRunner(After.class));
            assertTrue(worldIsBuilt);
            assertTrue(worldIsDestroyed);
        }


        static class After {
            After() {
                worldIsBuilt = true;
            }

            @Test
            void live_in_the_world() {

            }

            @AfterAll
            void destroy_the_world() {
                worldIsDestroyed = true;
            }
        }
    }

    public static class when_test_extends_another_test_class {
        static Long universeIsBuilt;
        static Long worldIsBuilt;
        static Long worldIsDestroyed;
        static Long universeIsDestroyed;

        @Test
        public void HavaRunner_calls_the_After_methods_in_the_test_class_hierarchy() {
            run(new HavaRunner(WorldCreator.class));
            Collection<Long> expectedOrder = Lists.newArrayList(
                universeIsBuilt,
                worldIsBuilt,
                worldIsDestroyed,
                universeIsDestroyed
            );
            Collection<Long> actualOrder = Sets.newTreeSet(Sets.newHashSet(
                checkNotNull(universeIsBuilt),
                checkNotNull(worldIsBuilt),
                checkNotNull(worldIsDestroyed),
                checkNotNull(universeIsDestroyed)
            ));
            assertArrayEquals(expectedOrder.toArray(), actualOrder.toArray());
        }


        static class WorldCreator extends UniverseCreator {
            WorldCreator() throws InterruptedException {
                Thread.sleep(1);
                worldIsBuilt = System.currentTimeMillis();
            }

            @Test
            void live_in_the_world() {

            }

            @AfterAll
            void destroy_the_world() throws InterruptedException {
                Thread.sleep(1);
                worldIsDestroyed = System.currentTimeMillis();
            }
        }

        static class UniverseCreator {
            UniverseCreator() throws InterruptedException {
                Thread.sleep(1);
                universeIsBuilt = System.currentTimeMillis();
            }

            @AfterAll
            void destroy_the_universe() throws InterruptedException {
                Thread.sleep(1);
                universeIsDestroyed = System.currentTimeMillis();
            }
        }
    }
}
