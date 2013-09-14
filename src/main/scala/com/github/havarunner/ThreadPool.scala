package com.github.havarunner

import java.util.concurrent.{ForkJoinPool, SynchronousQueue, TimeUnit, ThreadPoolExecutor}

private[havarunner] trait ThreadPool {
  implicit val executor: ForkJoinPool = new ForkJoinPool
}
