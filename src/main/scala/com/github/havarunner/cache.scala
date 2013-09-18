package com.github.havarunner

import scala.collection.mutable
import com.github.havarunner.Reflections._

private[havarunner] object SuiteCache {
  private val cache = new mutable.HashMap[Class[_ <: HavaRunnerSuite[_]], HavaRunnerSuite[_]] with mutable.SynchronizedMap[Class[_ <: HavaRunnerSuite[_]], HavaRunnerSuite[_]]

  def fromSuiteInstanceCache(suiteClass: Class[_ <: HavaRunnerSuite[_]]): HavaRunnerSuite[_] =
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

private[havarunner] object TestInstanceCache {
  private val cache = new mutable.HashMap[ScenarioAndClass, Any] with mutable.SynchronizedMap[ScenarioAndClass, Any]

  def fromTestInstanceCache(implicit testAndParameters: TestAndParameters): Any =
    testAndParameters.scenarioAndClass.clazz.synchronized {
      cache.get(testAndParameters.scenarioAndClass) getOrElse {
        val testInstance = instantiate(testAndParameters)
        cache(testAndParameters.scenarioAndClass) = testInstance
        testInstance
      }
    }
}