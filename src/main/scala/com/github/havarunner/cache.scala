package com.github.havarunner

import scala.collection.mutable
import com.github.havarunner.Reflections._
import com.github.havarunner.SuiteCache._
import com.github.havarunner.Parser.ParseResult

private[havarunner] object SuiteCache {
  private val cache = new mutable.HashMap[Class[_ <: HavaRunnerSuite[_]], HavaRunnerSuite[_]] with mutable.SynchronizedMap[Class[_ <: HavaRunnerSuite[_]], HavaRunnerSuite[_]]

  def suiteInstance(suiteClass: Class[_ <: HavaRunnerSuite[_]]): HavaRunnerSuite[_] =
    suiteClass.synchronized {
      cache.getOrElseUpdate(suiteClass, {
        val havaRunnerSuiteInstance: HavaRunnerSuite[_] = instantiateSuite(suiteClass)
        registerShutdownHook(havaRunnerSuiteInstance)
        havaRunnerSuiteInstance
      })
    }

  def instantiateSuite(suiteClass: Class[_ <: HavaRunnerSuite[_]]): HavaRunnerSuite[_] = {
    val noArgConstructor = suiteClass.getDeclaredConstructor()
    val havaRunnerSuiteInstance: HavaRunnerSuite[_] = ensureAccessible(noArgConstructor).newInstance()
    havaRunnerSuiteInstance
  }

  def registerShutdownHook(havaRunnerSuiteInstance: HavaRunnerSuite[_]) {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable() {
      def run() {
        havaRunnerSuiteInstance.afterSuite()
      }
    }))
  }
}

private[havarunner] object TestInstanceCache {
  val cache = new mutable.HashMap[ScenarioAndClass, Either[Exception, TestInstance]] with mutable.SynchronizedMap[ScenarioAndClass, Either[Exception, TestInstance]]
  val instanceGroupLocks = new mutable.HashMap[ScenarioAndClass, AnyRef] with mutable.SynchronizedMap[ScenarioAndClass, AnyRef]

  def instantiateTestClass(implicit testAndParameters: TestAndParameters, parseResult: ParseResult): Either[Exception, TestInstance] =
    newOrCachedInstance(
      testAndParameters,
      testAndParameters.partOf map suiteInstance,
      parseResult
    )

  def newOrCachedInstance(implicit testAndParameters: TestAndParameters, suiteOption: Option[HavaRunnerSuite[_]], parseResult: ParseResult): Either[Exception, TestInstance] =
    instanceGroupLocks.getOrElseUpdate(testAndParameters.groupCriterion, new Object).synchronized { // Sync with instance group. Different groups may run parallel.
      cache.
        get(testAndParameters.groupCriterion).
        getOrElse({
          val cachedValue = instantiate
          cache.update(testAndParameters.groupCriterion, cachedValue) // Do not use Map#getOrElseUpdate, because it would block unnecessarily
          cachedValue
        })
    }
}
