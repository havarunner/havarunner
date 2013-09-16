package com.github.havarunner

import scala.collection.mutable
import com.github.havarunner.annotation.PartOf

trait HavaRunnerSuite[T] {
  def suiteObject: T

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
