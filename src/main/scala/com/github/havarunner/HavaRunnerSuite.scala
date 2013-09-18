package com.github.havarunner


/**
 * Marks a test class as a suite.
 *
 * A suite is comprised of suite members that are annotated with [[com.github.havarunner.annotation.PartOf]].
 *
 * A suite is also a test, which means that it can contain tests.
 *
 * Suite members must be within the same package as the suite.
 *
 * @author Lauri Lehmijoki
 */
trait HavaRunnerSuite[T] {
  /**
   * With this method, implementations may provide a heavy-weight object to suite members.
   *
   * An example of a heavy-weight object is an HTTP server, which takes several seconds to start.
   *
   * @return the object that can be shared by all the suite members
   */
  def suiteObject: T

  /**
   * JVM will call this method in the shutdown hook phase (http://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)).
   */
  def afterSuite(): Unit
}