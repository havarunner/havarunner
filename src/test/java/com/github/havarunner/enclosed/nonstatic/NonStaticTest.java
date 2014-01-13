package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.HavaRunner;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;

public class NonStaticTest {
    static volatile int outerCalled;
    static volatile int innerCalled;

    @Test
    public void HavaRunner_supports_non_static_classes() {
        run(new HavaRunner(TopLevelAndNested.class));
        assertEquals(1, outerCalled);
        assertEquals(1, innerCalled);
    }
}
