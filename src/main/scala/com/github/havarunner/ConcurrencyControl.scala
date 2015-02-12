package com.github.havarunner

import java.util.concurrent.Semaphore
import com.github.havarunner.annotation.RunSequentially
import RunSequentially.SequentialityContext._
import scala.collection.mutable

private[havarunner] object ConcurrencyControl {
  val forParallelTests = new Semaphore(permits, true)

  def permits: Int = {
    System.getProperty("havarunner.maximum_parallelism", Runtime.getRuntime.availableProcessors().toString).toInt
  }

  val forTestsMarkedByTheDefaultContext = new Semaphore(1)
  val forTestsOfSameInstance = new mutable.HashMap[Any, Semaphore] with mutable.SynchronizedMap[Any, Semaphore]

  def withThrottle[T](body: => T)(implicit maybeSequential: MaybeSequential with InstanceGroup[_]) = {
    semaphore.acquire()
    try {
      body
    } finally {
      semaphore.release()
    }
  }

  def semaphore(implicit maybeSequential: MaybeSequential with InstanceGroup[_]) =
    maybeSequential.runSequentially map sequentialSemaphore getOrElse forParallelTests

  def sequentialSemaphore(runSequentially: RunSequentially)(implicit instanceGroup: InstanceGroup[_]): Semaphore =
    runSequentially.`with`() match {
      case TESTS_OF_SAME_INSTANCE       => forTestsOfSameInstance.getOrElseUpdate(instanceGroup.groupCriterion, new Semaphore(1))
      case TESTS_MARKED_BY_THIS_CONTEXT => forTestsMarkedByTheDefaultContext
    }
}
