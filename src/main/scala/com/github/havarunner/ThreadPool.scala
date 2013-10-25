package com.github.havarunner

import java.util.concurrent.ForkJoinPool

private[havarunner] trait ThreadPool {
  implicit val executor: ForkJoinPool = new ForkJoinPool(
    ConcurrencyControl.concurrencyLevel,
    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null,
    true
  )
}
