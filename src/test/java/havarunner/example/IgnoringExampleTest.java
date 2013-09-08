package havarunner.example;

import havarunner.HavaRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HavaRunner.class)
public class IgnoringExampleTest {

    @Test
    @Ignore
    void ignored_test() {
        throw new RuntimeException(HavaRunner.class.getSimpleName()+" will not run this test, because it is @Ignored");
    }
}
