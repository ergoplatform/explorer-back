package org.ergoplatform.explorer

object Pools {

  val dbCallsFixedThreadPool = java.util.concurrent.Executors.newFixedThreadPool(15)

  val grabberPool = java.util.concurrent.Executors.newFixedThreadPool(5)

  def shutdown = {
    dbCallsFixedThreadPool.shutdown()
    grabberPool.shutdown()
  }
}
