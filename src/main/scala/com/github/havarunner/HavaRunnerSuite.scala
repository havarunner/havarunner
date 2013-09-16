package com.github.havarunner

import scala.collection.mutable
import com.github.havarunner.annotation.PartOf

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

private[havarunner] object HavaRunnerSuite {
  val cache = new mutable.HashMap[Class[_ <: HavaRunnerSuite[_]], HavaRunnerSuite[_]] with mutable.SynchronizedMap[Class[_ <: HavaRunnerSuite[_]], HavaRunnerSuite[_]]

  def suiteOption(implicit clazz: Class[_]): Option[HavaRunnerSuite[_]] =
    Option(clazz.getAnnotation(classOf[PartOf])) map {
      partOfAnnotation =>
        val suiteClass = partOfAnnotation.value()
        doCacheLookup(suiteClass)
    }

  private def doCacheLookup(suiteClass: Class[_ <: HavaRunnerSuite[_]]): HavaRunnerSuite[_] =
    suiteClass.synchronized {
      cache.get(suiteClass) getOrElse {
        val noArgConstructor = suiteClass.getDeclaredConstructor()
        noArgConstructor.setAccessible(true)
        val havaRunnerSuiteInstance: HavaRunnerSuite[_] = noArgConstructor.newInstance()
        cache(suiteClass) = havaRunnerSuiteInstance
        Runtime.getRuntime.addShutdownHook(new Thread(new Runnable() {
          def run() {
            havaRunnerSuiteInstance.afterSuite()
          }
        }))
        havaRunnerSuiteInstance
      }
    }
}
