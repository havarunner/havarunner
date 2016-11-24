# Changelog

This project uses [Semantic Versioning](http://semver.org)

## 1.2.1

* Fix missing support of TestRule annotations

## 1.2.0

* Add support for controlling the maximum level of parallelism

## 1.1.4

* Add Java 8 support

## 1.1.3

* Show a correct failure report when a test throws an exception that is not
  expected

## 1.1.2

* Fix bug in `@Test(expected = SomeException.class)`

## 1.1.1

* Print the reason of failed assumption

## 1.1.0

* Add support for covariant suite objects and scenarios

  From now on, you can use interfaces in the constructors of your test
  classes.

  See <https://github.com/havarunner/havarunner/pull/3> for discussion.

  Thanks to <https://github.com/charith-qubit> for implementing this!

## 1.0.1

* Fail the test if its class has more than one @Scenario methods

## 1.0.0

* This is the same release as 1.0.0-RC3

## 1.0.0-RC3

* Minor internal refactorings

## 1.0.0-RC2

* Fix bug where a failure in @Before broke the order of test events

## 1.0.0-RC1

* No functional changes since the previous release. (Prepare for the 1.0.0 release.)

## 0.12.7

* Run tests of same group in undeterministic order (revert 0.12.6)

## 0.12.6

* Run tests of same group in the parsed order

## 0.12.5

* Support @Ignore in the enclosing class

## 0.12.4

* Speed up multiscenario tests

## 0.12.3

* Synchronise instantiation against the test class

## 0.12.2

* Run child @Afters before parent @Afters

## 0.12.1

* Make @RunSequentially(because) optional
* Run @AfterAlls within the throttle

## 0.12.0

* Rename JEAN_LUC_PICARD -> TESTS_MARKED_BY_THIS_CONTEXT
 * This is a **backward incompatible change**. Migrate by renaming
   `JEAN_LUC_PICARD` –> `TESTS_MARKED_BY_THIS_CONTEXT`
* Support @RunSequentially in HavaRunnerSuite

## 0.11.4

* Fix synchronisation

  Instantiate the object and run the tests within the same throttling session.

## 0.11.3

* Set default sequentiality context to `JEAN_LUC_PICARD`

## 0.11.2

* Reduce noise when reporting suite

## 0.11.1

* Support failed assumptions in @Before methods

## 0.11.0

* Add `@RunSequentially(with = TESTS_OF_SAME_INSTANCE)`

## 0.10.9

* Re-build 0.10.8 with JDK 7, not JDK 8

## 0.10.8

* Require reason when @RunSequentially

## 0.10.7

* Report multiple invalidations at once
* Reduce noise in suite reports

## 0.10.6

* Improve speed by rewriting the scheduler as non-blocking
* Improve suite reporting

## 0.10.5

* Fix bug where HavaRunner ran the same test multiple times

## 0.10.4

* Rename ContainsNonStaticInnerClassException -> NonStaticInnerClassException

## 0.10.3

* Include inner classes of suite members in the suite

## 0.10.2

* Fail if the enclosed class is non-static

## 0.10.1

* Show a helpful error if parallel test uses @Before or @After

## 0.10.0

* Support @Before and @After when sequential

## 0.9.5

* Non-blocking sequential tests

## 0.9.4

* Use ForkJoinPool asyncMode = true

## 0.9.3

* Do not fail if a test assumption fails and there is a `@Rule`

## 0.9.2

* Do not run the whole test set sequentially if one of the tests is marked with `@RunSequentially`

## 0.9.1

* Add missing support for @Test(timeout = x)

## 0.9.0

* Support JUnit
  [TestRules](http://junit-team.github.io/junit/javadoc/4.10/org/junit/rules/TestRule.html)

## 0.8.7

* Remove started/finished stdout spam
* Internal refactorings

## 0.8.6

* Run the `@AfterAll` methods in parallel

## 0.8.5

* Invoke the assumption-failed API, not the ignore API, when an assumption
  failed

## 0.8.4

* Do not include abstract classes in the test class set

## 0.8.3

* Tolerate assumeThat calls in constructor when @AfterAll is present

## 0.8.2

* Allow static inner classes to be suite members

## 0.8.1

* When running suite, find the tests whose suite membership is declared in an
  abstract class

## 0.8.0

* Support the JUnit assumption API
* Allow abstract classes to define suite membership

## 0.7.1

* Run suite members sequentially

## 0.7.0

* Support recursive `static class` test definitions

## 0.6.1

* Do not fail silently if class-loading fails

## 0.6.0

* Print suite members to stdout before running

## 0.5.3

* Add missing docs into public classes and interfaces

## 0.5.2

* Fail if the suite member is not within the same package as the suite

## 0.5.1

* Support `@org.junit.Ignore` on class

## 0.5.0

* Add suite support
