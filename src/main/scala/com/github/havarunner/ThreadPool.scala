package com.github.havarunner

import java.util.concurrent.{SynchronousQueue, TimeUnit, ThreadPoolExecutor}

private[havarunner] trait ThreadPool {
  val executor = new ThreadPoolExecutor(
    0, ConcurrencyControl.concurrencyLevel,
    60L, TimeUnit.SECONDS,
    new SynchronousQueue[Runnable],
    new ThreadPoolExecutor.CallerRunsPolicy()
  )
}
