# HavaRunner – a strongly opinionated JUnit runner

[![Build
Status](https://travis-ci.org/havarunner/havarunner.png?branch=master)](https://travis-ci.org/havarunner/havarunner)

## Features

* **Once instance per test**
 * Do your computation-intensive setup in the constructor of the test class
 * Write easy-to-reason-about test classes that rely on `final` instance fields
* **Scenarios**
 * Run the same test against multiple scenarios
* **Enclosed tests**
 * Group tests with static inner classes (like with `org.junit.experimental.runners.Enclosed`)
* **Run tests in parallel by default**
* **Strict coding conventions**
 * HavaRunner requires snake\_case in test methods
* **Reduce syntactic noise**
 * HavaRunner lets you omit the `public` access modifier from the methods

## Usage

````java
@RunWith(HavaRunner.class)
public class HelloWorldTest {

    @Test
    void HavaRunner_greets_the_world() {
        System.out.println("Hello world");
    }
}
````

### Run sequentially

If your tests do not thrive in the concurrent world, you can instruct HavaRunner
to run them sequentially:

````java
@RunWith(HavaRunner.class)
@RunSequentially
public class HelloWorldTest {
  // here be test code
}
````

Concurrency problems imply a [Code
Smell](http://en.wikipedia.org/wiki/Code_smell). If you see sequentially-run
HavaRunner tests in the codebase, try to understand why they cannot be run
concurrently. From this understanding you might gain valuable insights into the
architectural problems of the system.

### Full code examples

[Here](https://github.com/havarunner/havarunner/tree/master/src/test/java/havarunner/example)
you can find complete code examples.

## Supported JUnit annotations

HavaRunner supports only a limited set of JUnit annotations. Here they are:

| Annotation | Semantics | Comment |
| ---------- | --------- | ------- |
| `@org.junit.Test` | Same as in JUnit | This fellow you already know. |
| `@org.junit.After` | Methods with this annotation will be run after **all** the tests in the class. Because of this, its semantics resemble those of `@org.junit.AfterClass`.  | This will be replaced by `@com.github.havarunner.annotation.AfterAll` |

HavaRunner supports none of the other JUnit annotations.

## Tests

`sbt test`

## License

See the LICENSE file. It's MIT.
