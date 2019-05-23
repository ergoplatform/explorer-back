package org.ergoplatform.explorer

import cats.effect.IO
import org.ergoplatform.explorer.services._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait Services { self: DbTransactor with Configuration with OffChainMonitoring =>

  val servicesEc: ExecutionContextExecutor = ExecutionContext.fromExecutor(Pools.dbCallsFixedThreadPool)

  val blocksService = new BlocksServiceIOImpl[IO](transactor, servicesEc)
  val txService = new TransactionsServiceIOImpl[IO](transactor, offChainStorage, servicesEc, cfg.grabber)
  val addressesService = new AddressesServiceIOImpl[IO](transactor, offChainStorage, servicesEc, cfg.protocol)
  val statsService = new StatsServiceIOImpl[IO](cfg.protocol)(transactor, servicesEc)
  val minerService = new MinerServiceIOImpl[IO](transactor, servicesEc)
}
