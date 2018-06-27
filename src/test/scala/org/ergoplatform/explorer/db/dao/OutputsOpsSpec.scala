package org.ergoplatform.explorer.db.dao

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class OutputsOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import OutputsOps._

  lazy val transactor: Transactor[IO] = xa

  it should "perform queries on outputs table" in new {
    check(insert)
    check(findAllByTxId(""))
    check(findAllByTxsId(NonEmptyList.fromListUnsafe(List(""))))
    check(findByHash(""))
    check(searchByHash(""))
    check(sumOfAllUnspentOutputs)
    check(findAllByTxIdWithSpent(""))
    check(findAllByTxsIdWithSpent(NonEmptyList.fromListUnsafe(List(""))))
  }

}
