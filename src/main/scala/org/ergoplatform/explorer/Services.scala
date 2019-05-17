package org.ergoplatform.explorer

import cats.effect.IO
import org.ergoplatform.explorer.services._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait Services { self: DbTransactor with Configuration =>

  val servicesEc: ExecutionContextExecutor = ExecutionContext.fromExecutor(Pools.dbCallsFixedThreadPool)

  val blocksService = new BlocksServiceIOImpl[IO](transactor, servicesEc)
  val txService = new TransactionsServiceIOImpl[IO](transactor, servicesEc, cfg.grabber)
  val addressesService = new AddressesServiceIOImpl[IO](transactor, servicesEc)
  val statsService = new StatsServiceIOImpl[IO](cfg.protocol)(transactor, servicesEc)
  val minerService = new MinerServiceIOImpl[IO](transactor, servicesEc)
}
