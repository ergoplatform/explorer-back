package org.ergoplatform.explorer

import cats.effect.IO
import org.ergoplatform.explorer.services.BlocksServiceImpl

import scala.concurrent.ExecutionContext

trait Services { self: DbTransactor with Configuration =>

  val servicesEc = ExecutionContext.fromExecutor(java.util.concurrent.Executors.newFixedThreadPool(10))

  val blocksService = new BlocksServiceImpl[IO](transactor, servicesEc)
}
