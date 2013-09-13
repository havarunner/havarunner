# HavaRunner – a JUnit runner

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
* **Reduce syntactic noise**
 * HavaRunner lets you omit the `public` access modifier from the methods

## Install

````xml
<dependency>
  <groupId>com.github.havarunner</groupId>
  <artifactId>havarunner</artifactId>
  <version>0.4.0</version>
</dependency>
````

## Usage

### Hello world

````java
@RunWith(HavaRunner.class)
public class HelloWorldTest {

    final Object world;

    HelloWorldTest() {
        // You can do your setup in the constructor. Benefit from immutable objects!
        world = "hello";
    }

    @Test
    void HavaRunner_greets_the_world() {
        System.out.println("hello "+world);
    }

    @AfterAll
    void destroy() {
        // This method will be invoked after all the tests in the class
        world = null;
    }
}
````

### Run the same test against multiple scenarios

HavaRunner lets you model your use cases with scenarios.

From Merriam-Webster:

<blockquote>Scenario – a description of what could possibly happen</blockquote>

You can use scenarios by adding a static `@Scenarios` method and a constructor
that takes in one argument. HavaRunner will then call your test methods once
for each scenario.

````java
@RunWith(HavaRunner.class)
public class LoginPageTest {

    final User user;
    final Object loginPage;

    LoginPageTest(User user) {
        this.user = user;
        loginPage = "here be html";
    }

    @Test
    void login_page_looks_the_same_for_all_users() {
        if (user == User.ADMIN) {
            assertNotNull(loginPage);
        } else if (user == User.ANONYMOUS) {
            assertNotNull(loginPage);
        } else {
            throw new IllegalArgumentException("Unrecognised user " + user);
        }
    }

    enum User {
        ADMIN,
        ANONYMOUS
    }

    @Scenarios
    static Collection<User> users() {
        Collection<User> users = new ArrayList<>();
        Collections.addAll(users, User.values());
        return users;
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

[Here](https://github.com/havarunner/havarunner/tree/master/src/test/java/com/github/havarunner/example)
you can find complete code examples.

## Supported JUnit annotations

HavaRunner supports only a limited set of JUnit annotations. Here they are:

| Annotation | Semantics | Comment |
| ---------- | --------- | ------- |
| `@org.junit.Test` | Same as in JUnit | This fellow you already know. |

HavaRunner supports none of the other JUnit annotations.

## Tests

`sbt test`

## License

See the LICENSE file. It's MIT.
