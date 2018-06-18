package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.utils.generators.HeadersGen
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class HeadersOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import HeadersOps._

  lazy val transactor: Transactor[IO] = xa

  val h = HeadersGen.headerGen("test", 1).sample.get

  it should "perform queries on headers table" in {
    check(select(""))
    check(selectByParentId(""))
    check(selectLast(10))
    check(selectHeight(""))
    check(list(0, 20, "id", "ASC", 0L, 100L))
    check(insert)
    check(update)
    check(count(0L, 0L))
  }

}
