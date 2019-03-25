package org.ergoplatform.explorer

import java.util.concurrent.ExecutorService

object Pools {

  val dbCallsFixedThreadPool: ExecutorService = java.util.concurrent.Executors.newFixedThreadPool(15)

  val grabberPool: ExecutorService = java.util.concurrent.Executors.newFixedThreadPool(5)

  def shutdown(): Unit = {
    dbCallsFixedThreadPool.shutdown()
    grabberPool.shutdown()
  }
}
