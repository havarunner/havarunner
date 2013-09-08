# HavaRunner – a strongly opinionated JUnit runner

[![Build
Status](https://travis-ci.org/laurilehmijoki/HavaRunner.png?branch=master)](https://travis-ci.org/laurilehmijoki/HavaRunner)

## Features

* Strict coding conventions (HavaRunner requires snake\_case in test methods)
* Scenarios (run the same test against multiple scenarios)
* Enclosed tests (group tests with static inner classes)
* Tests are run in parallel by default

## Usage

````java
package helloworld;

import havarunner.HavaRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

@RunWith(HavaRunner.class)
public class HelloWorldTest {

    @Test
    void HavaRunner_greets_the_world() {
        assertNotNull("Hello world");
    }
}
````

## Tests

`mvn test`

## License

See the LICENSE file. It's MIT.
