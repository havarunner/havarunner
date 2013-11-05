# HavaRunner

[![Build
Status](https://travis-ci.org/havarunner/havarunner.png?branch=master)](https://travis-ci.org/havarunner/havarunner)

## Features

* **Run tests in parallel by default**
 * Speed up development cycles with faster tests
* **Suites**
 * Group your tests by annotating them as `@PartOf` a suite
* **Scenarios**
 * Run the same test against multiple scenarios
* **Enclosed tests**
 * Group tests with static inner classes (like with `org.junit.experimental.runners.Enclosed`)
* **Once instance per test**
 * Do your computation-intensive setup in the constructor of the test class
 * Write easy-to-reason-about test classes that rely on `final` instance fields
* **Reduce syntactic noise**
 * HavaRunner lets you omit the `public` access modifier from the methods
* **Non-blocking**
 * HavaRunner is built on Scala 2.10 futures, and it's run model is completely
   asynchronous

## Install

````xml
<dependency>
  <groupId>com.github.havarunner</groupId>
  <artifactId>havarunner</artifactId>
  <!-- Here you can find the latest version number: -->
  <!-- http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.havarunner%22%20AND%20a%3A%22havarunner%22 -->
  <version>x.y.z</version>
  <scope>test</scope>
</dependency>
<dependency>
  <!-- HavaRunner lets you manage the JUnit dependency. -->
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>4.11</version> <!-- Any JUnit above 4.10 should do. -->
  <scope>test</scope>
</dependency>
<dependency>
  <!-- HavaRunner lets you manage the Guava dependency. -->
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>14.0.1</version> <!-- HavaRunner needs the v.14 or higher. -->
  <scope>test</scope>
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

### Suites

Suites are a way to group tests and pass them shared data.

Suites are comprised of *suite members*, and HavaRunner instantiates
each member by passing it the `suiteObject`.

Because suites are instantiated only once, their constructors are an ideal place
to perform heavy setup logic, such as launching a web server.

JVM will call the `afterSuite` method of the suite in the shutdown hook.

````java
package your.app;

@RunWith(HavaRunner.class)
public class WebApplicationSuiteTest implements HavaRunnerSuite<WebServer> {

    final WebServer webServer;

    public WebApplicationSuiteTest() {
        this.webServer = new WebServer(); // Instantiate a heavy object
    }

    @Override
    public WebServer suiteObject() {
        return webServer;
    }

    @Override
    public void afterSuite() { // JVM calls this method in the shutdown hook
        webServer.shutDown();
    }
}
````

````java
// The suite member must be in the same package or a subpackage of the suite.
package your.app;

@RunWith(HavaRunner.class)
@PartOf(WebApplicationSuiteTest.class)
public class RestTest {

    private final WebServer webServer;

    // HavaRunner will pass the suite object to the constructor
    RestTest(WebServer webServer) {
        this.webServer = webServer;
    }

    @Test
    void rest_api_responds_200() {
        assertEquals(200, webServer.httpStatus(""));
    }
}
````

#### Scenario tests in a suite

````java
@RunWith(HavaRunner.class)
@PartOf(WebApplicationSuiteTest.class)
public class RestForDifferentUsersTest {

    private final WebServer webServer;
    private final String user;

    // The first arg is the suite object, the second one is the scenario object.
    public RestForDifferentUsersTest(WebServer webServer, String user) {
        this.webServer = webServer;
        this.user = user;
    }

    @Test
    void users_receive_personal_response() {
        assertEquals(
            "<html><body><title>hello "+user+"</title></body></html>",
            webServer.htmlDocument("/", user)
        );
    }

    @Scenarios
    static ArrayList<String> users() {
        ArrayList<String> users = new ArrayList<>();
        users.add("Kalevi");
        users.add("Pasi");
        return users;
    }
}
````

### Enclosed tests

````java
@RunWith(HavaRunner.class)
public class EnclosedExampleTest {

    static class enclosed_example {
        @Test
        void using_static_classes_within_the_test_class_is_a_way_to_group_tests() {
        }
    }

    static class parent {
        @Test
        void you_can_use_inner_classes_recursively() {
            System.out.println("hello recursive example @ " + getClass().getSimpleName());
        }
        static class child {
            @Test
            void you_can_use_inner_classes_recursively() {
                System.out.println("hello recursive example @ " + getClass().getSimpleName());
            }
            static class grandchild {
                @Test
                void you_can_use_inner_classes_recursively() {
                    System.out.println("hello recursive example @ " + getClass().getSimpleName());
                }
            }
        }
    }
}
````

### Run sequentially

If your tests do not thrive in the concurrent world, you can instruct HavaRunner
to run them sequentially:

````java
@RunWith(HavaRunner.class)
@RunSequentially(because = "this test uses a backend that does not support parallel usage")
public class HelloWorldTest {
  // here be test code
}
````

Concurrency problems imply a [Code
Smell](http://en.wikipedia.org/wiki/Code_smell). If you see sequentially-run
HavaRunner tests in the codebase, try to understand why they cannot be run
concurrently. From this understanding you might gain valuable insights into the
architectural problems of the system.

As a corollary of the above arguments, the `because` attribute of the
`@RunSequentially` annotation is mandatory. The attribute asks you to leave a
helpful hint to the programmers who will be working with the test in the future.
If a system does not support concurrency, it is often difficult to find out the
reason. Let the `because` attribute help you in documenting these causes.

### Using the JUnit assumption API

````java
@RunWith(HavaRunner.class)
public class AssumeThatExample {
    boolean weHaveFlt = false;

    @Test
    void when_we_fare_the_galaxies() {
        org.junit.Assume.assumeTrue(weHaveFlt); // HavaRunner ignores this test, because the assumption does not hold
    }
}
````

### Full code examples

[Here](https://github.com/havarunner/havarunner/tree/master/src/test/java/com/github/havarunner/example)
you can find complete code examples.

## Project goals

* Minimise or eliminate the need to describe test configurations in Maven
  pom.xml
* Create a tool for fast tests – run everything in parallel by default
* Encourage users to write easy-to-understand tests that build on immutable data
  and intuitive abstractions such as suite and scenario
* Provide a model for ensuring that all the tests in the codebase are run in CI

## Supported JUnit annotations

HavaRunner supports only a limited set of JUnit annotations. Here they are:

| Annotation | Semantics | Comment |
| ---------- | --------- | ------- |
| `@org.junit.Test` | Same as in JUnit | This fellow you already know. |
| `@org.junit.Ignore` | Same as in JUnit | This fellow you already know. |
| `@org.junit.Rule` | Same as in JUnit | HavaRunner supports only TestRules, not Method rules. |
| `@org.junit.Before` | Same as in JUnit | Supported only when the test is `@RunSequentially`. |
| `@org.junit.After` | Same as in JUnit | Supported only when the test is `@RunSequentially`. |

HavaRunner supports none of the other JUnit annotations.

## Tests

`sbt test`

## License

See the LICENSE file. It's MIT.

Author Lauri Lehmijoki.
