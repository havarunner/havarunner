package com.github.havarunner

import java.util.concurrent.Semaphore
import com.github.havarunner.annotation.RunSequentially
import RunSequentially.SequentialityContext._
import scala.collection.mutable

private[havarunner] object ConcurrencyControl {
  val forParallelTests = new Semaphore(Runtime.getRuntime.availableProcessors(), true)
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

  private def sequentialSemaphore(runSequentially: RunSequentially)(implicit instanceGroup: InstanceGroup[_]): Semaphore =
    runSequentially.`with`() match {
      case TESTS_OF_SAME_INSTANCE       => forTestsOfSameInstance.getOrElseUpdate(instanceGroup.criterion, new Semaphore(1))
      case TESTS_MARKED_BY_THIS_CONTEXT => forTestsMarkedByTheDefaultContext
    }
}
