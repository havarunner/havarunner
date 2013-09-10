package havarunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static havarunner.TestHelper.run;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class AfterTest {

    public static class when_test__fails {
        static boolean worldIsBuilt = false;
        static boolean worldIsDestroyed = false;

        @Test
        public void HavaRunner_calls_the_After_method_even_if_the_test_throws_an_exception() {
            run(new HavaRunner(AfterFailingTest.class));
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

            @org.junit.After
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

            @org.junit.After
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

            @org.junit.After
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

            @org.junit.After
            void destroy_the_universe() throws InterruptedException {
                Thread.sleep(1);
                universeIsDestroyed = System.currentTimeMillis();
            }
        }
    }
}
