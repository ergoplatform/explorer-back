package org.ergoplatform.explorer.db.dao

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class InputsOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import InputsOps._

  lazy val transactor: Transactor[IO] = xa

  it should "perform queries on inputs table" in new {
    check(insert)
    check(findAllByTxId(""))
    check(findAllByTxsId(NonEmptyList.fromListUnsafe(List(""))))
    check(findAllByTxIdWithValue(""))
    check(findAllByTxsIdWithValue(NonEmptyList.fromListUnsafe(List(""))))
  }
}
