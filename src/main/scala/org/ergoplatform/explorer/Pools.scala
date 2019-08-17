package org.ergoplatform.explorer

import java.util.concurrent.ExecutorService

object Pools {

  val dbCallsFixedThreadPool: ExecutorService = java.util.concurrent.Executors.newFixedThreadPool(15)

  val onChainMonitoringPool: ExecutorService = java.util.concurrent.Executors.newFixedThreadPool(5)

  val offChainMonitoringPool: ExecutorService = java.util.concurrent.Executors.newFixedThreadPool(3)

}
