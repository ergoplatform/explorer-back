package org.ergoplatform.explorer

import cats.effect.IO
import cats.effect.concurrent.Ref
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.explorer.services._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait Services { self: DbTransactor with Configuration =>

  val txPoolRef: Ref[IO, TransactionsPool]

  val servicesEc: ExecutionContextExecutor = ExecutionContext.fromExecutor(Pools.dbCallsFixedThreadPool)

  val blocksService = new BlocksServiceIOImpl[IO](transactor, servicesEc)
  val txService = new TransactionsServiceIOImpl[IO](transactor, txPoolRef, servicesEc, cfg.grabber)
  val addressesService = new AddressesServiceIOImpl[IO](transactor, txPoolRef, servicesEc, cfg.protocol)
  val statsService = new StatsServiceIOImpl[IO](cfg.protocol)(transactor, servicesEc)
  val minerService = new MinerServiceIOImpl[IO](transactor, servicesEc)
}
