package com.github.havarunner

import java.util.concurrent.{ScheduledFuture, TimeUnit, Executors, Semaphore}
import com.github.havarunner.annotation.RunSequentially
import com.github.havarunner.SystemLoad.querySystemLoad
import RunSequentially.SequentialityContext._
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import java.util.concurrent.atomic.{AtomicReference, AtomicBoolean}

private[havarunner] object ConcurrencyControl {
  val availableProcessors = Runtime.getRuntime.availableProcessors()
  val forParallelTests = new Semaphore(
    availableProcessors - querySystemLoad max 1, // Adapt to system load before starting to run any tests
    true
  )
  val forTestsMarkedByTheDefaultContext = new Semaphore(1)
  val forTestsOfSameInstance = new mutable.HashMap[Any, Semaphore] with mutable.SynchronizedMap[Any, Semaphore]

  def withThrottle[T](body: => T)(implicit maybeSequential: MaybeSequential with InstanceGroup[_]) = {
    val semaphores = resolveSemaphores
    semaphores.foreach(_.acquire())
    try {
      body
    } finally {
      semaphores.foreach(_.release())
    }
  }

  def resolveSemaphores(implicit maybeSequential: MaybeSequential with InstanceGroup[_]): Seq[Semaphore] =
    maybeSequential.runSequentially map sequentialSemaphores getOrElse (forParallelTests :: Nil)

  def sequentialSemaphores(runSequentially: RunSequentially)(implicit instanceGroup: InstanceGroup[_]): Seq[Semaphore] =
    runSequentially.`with`() match {
      case TESTS_OF_SAME_INSTANCE       => forParallelTests :: forTestsOfSameInstance.getOrElseUpdate(instanceGroup.groupCriterion, new Semaphore(1)) :: Nil
      case TESTS_MARKED_BY_THIS_CONTEXT => forTestsMarkedByTheDefaultContext :: Nil
    }

  object AutoScalingHelper {
    val threadPool = Executors.newSingleThreadScheduledExecutor()
    val scalingJob = new AtomicReference[Option[ScheduledFuture[_]]](None)
    
    def startAutomaticScaling {
      synchronized {
        scalingJob.get() match {
          case None =>
            val job = threadPool.scheduleWithFixedDelay(new LoadAdapter, 1, 1, TimeUnit.SECONDS)
            scalingJob.set(Some(job))
        }
      }
    }

    def stopAutomaticScaling {
      synchronized {
        scalingJob.get() match {
          case Some(job) =>
            job.cancel(true)
            scalingJob.set(None)
        }
      }
    }

    def adaptToSystemLoad(
                           systemLoad: Int = querySystemLoad,
                           availableProcessors: Int = availableProcessors,
                           semaphore: Semaphore
                           ) {
      val systemIsOverloaded = systemLoad > availableProcessors
      if (systemIsOverloaded ) {
        println(s"[HavaRunner] The system is overloaded (load $systemLoad, permits ${semaphore.availablePermits()}, queue length ${semaphore.getQueueLength}, available processors $availableProcessors)")
        semaphore.tryAcquire()
      } else {
        println(s"[HavaRunner] Increasing the concurrency level (load $systemLoad, permits ${semaphore.availablePermits()}, queue length ${semaphore.getQueueLength}, available processors $availableProcessors)")
        semaphore.release()
      }
    }

    class LoadAdapter extends Runnable {
      def run() =
        adaptToSystemLoad(semaphore = forParallelTests)
    }

  }

  object Implicits {
    // Use an unlimited thread pool. Let the semaphores take care of throttling.
    implicit lazy val global: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  }
}