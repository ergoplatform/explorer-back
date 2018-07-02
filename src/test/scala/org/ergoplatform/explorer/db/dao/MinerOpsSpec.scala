package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class MinerOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import MinerOps._

  lazy val transactor: Transactor[IO] = xa

  it should "perform queries on miners table" in {
    check(insert)
    check(update)
    check(delete(""))
    check(find(""))
    check(searchAddress(""))
  }
}
