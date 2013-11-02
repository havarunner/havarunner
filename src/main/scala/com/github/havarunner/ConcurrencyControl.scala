package com.github.havarunner

import java.util.concurrent.Semaphore

private[havarunner] object ConcurrencyControl {
  val concurrentSequentialTests = 1
  val concurrencyLevel = Runtime.getRuntime.availableProcessors() + concurrentSequentialTests
  val forParallelTests = new Semaphore(concurrencyLevel - concurrentSequentialTests, true)
  val forSequentialTests = new Semaphore(concurrentSequentialTests)

  def withThrottle[T](body: => T)(implicit maybeSequential: MaybeSequential) = {
    semaphore.acquire()
    try {
      body
    } finally {
      semaphore.release()
    }
  }

  private def semaphore(implicit maybeSequential: MaybeSequential) =
    if (maybeSequential.runSequentially)
      forSequentialTests
    else
      forParallelTests
}
