# Changelog

This project uses [Semantic Versioning](http://semver.org)

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
