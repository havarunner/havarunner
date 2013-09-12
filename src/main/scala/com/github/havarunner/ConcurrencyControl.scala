package com.github.havarunner

import java.util.concurrent.Semaphore

private[havarunner] object ConcurrencyControl {
  val concurrencyLevel = Runtime.getRuntime.availableProcessors()
  private val semaphore = new Semaphore(concurrencyLevel, true)

  def withThrottle[T](operation: Operation[T]) = {
    semaphore.acquire()
    try {
      operation.run
    } finally {
      semaphore.release()
    }
  }
}
