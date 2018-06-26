package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class BlockListOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import BlockListOps._

  lazy val transactor: Transactor[IO] = xa

  it should "perform queries on joined header-info table" in {
    count(0L, 0L)
    list(0,0, "", "", 0L, 0L)
  }
}
