package com.github.havarunner

import scala.collection.mutable
import com.github.havarunner.Reflections._

private[havarunner] object SuiteCache {
  private val cache = new mutable.HashMap[Class[_ <: HavaRunnerSuite[_]], HavaRunnerSuite[_]] with mutable.SynchronizedMap[Class[_ <: HavaRunnerSuite[_]], HavaRunnerSuite[_]]

  def fromSuiteInstanceCache(suiteClass: Class[_ <: HavaRunnerSuite[_]]): HavaRunnerSuite[_] =
    suiteClass.synchronized {
      cache.getOrElseUpdate(suiteClass, {
        val noArgConstructor = suiteClass.getDeclaredConstructor()
        noArgConstructor.setAccessible(true)
        val havaRunnerSuiteInstance: HavaRunnerSuite[_] = noArgConstructor.newInstance()
        Runtime.getRuntime.addShutdownHook(new Thread(new Runnable() {
          def run() {
            havaRunnerSuiteInstance.afterSuite()
          }
        }))
        havaRunnerSuiteInstance
      })
    }
}

private[havarunner] object TestInstanceCache {
  private val cache = new mutable.HashMap[ScenarioAndClass, Any] with mutable.SynchronizedMap[ScenarioAndClass, Any]

  def testInstance(implicit testAndParameters: TestAndParameters): Any =
    testAndParameters.scenarioAndClass.clazz.synchronized {
      cache.getOrElseUpdate(testAndParameters.scenarioAndClass, {
        val testInstance = instantiate(
          testAndParameters.partOf,
          testAndParameters.scenarioAndClass.scenarioOption,
          testAndParameters.scenarioAndClass.clazz
        )
        cache(testAndParameters.scenarioAndClass) = testInstance
        testInstance
      })
    }
}
