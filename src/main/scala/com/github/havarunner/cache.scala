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

  private[this] def instantiateSuite(suiteClass: Class[_ <: HavaRunnerSuite[_]]): HavaRunnerSuite[_] = {
    val noArgConstructor = suiteClass.getDeclaredConstructor()
    val havaRunnerSuiteInstance: HavaRunnerSuite[_] = ensureAccessible(noArgConstructor).newInstance() // Todo might result in an exception. Use Either[Exception, Any].
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

  def instantiateTestClass(implicit instantiationParams: InstantiationParams, parseResult: ParseResult): Either[Exception, TestInstance] =
    instanceGroupLocks.getOrElseUpdate(instantiationParams.groupCriterion, new Object).synchronized { // Sync with instance group. Different groups may run parallel.
      cache.
        get(instantiationParams.groupCriterion).
        getOrElse({
        val cachedValue = instantiate
        cache.update(instantiationParams.groupCriterion, cachedValue) // Do not use Map#getOrElseUpdate, because it would block unnecessarily
        cachedValue
      })
    }
}
