package com.github.havarunner

import java.util.concurrent.ForkJoinPool

private[havarunner] trait ThreadPool {
  implicit val executor: ForkJoinPool = new ForkJoinPool(
    Runtime.getRuntime().availableProcessors(),
    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null,
    true
  )
}
