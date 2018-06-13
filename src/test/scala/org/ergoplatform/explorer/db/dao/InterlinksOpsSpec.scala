package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class InterlinksOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import InterlinksOps._

  lazy val transactor: Transactor[IO] = xa

  it should "perform queries on interlinks table" in new {
    check(findAllByBlockId(""))
    check(insertTupled)
  }
}
