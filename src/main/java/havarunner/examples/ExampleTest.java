package havarunner.examples;

import havarunner.HavaRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(HavaRunner.class)
public class ExampleTest {

    @Test
    void HavaRunner_requires_snake_cased_methods() {
        assertTrue(true);
    }

    @Test
    public void HavaRunner_requires_package_private_method_access_modifiers() {
        assertTrue(true);
    }
}
