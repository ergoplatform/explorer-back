package org.ergoplatform.explorer

object Pools {

  val dbCallsFixedThreadPool = java.util.concurrent.Executors.newFixedThreadPool(15)

  def shutdown = dbCallsFixedThreadPool.shutdown()
}
