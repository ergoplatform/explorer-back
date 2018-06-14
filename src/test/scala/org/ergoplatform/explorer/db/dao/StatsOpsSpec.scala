package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class StatsOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import StatsOps._

  lazy val transactor: Transactor[IO] = xa

  it should "perform queries on stats table" in new {
    check(findLast(10))
    check(insert)
    check(difficultiesSumSince(0L))
  }

}
