package org.ergoplatform.explorer.db.dao


import cats.data.NonEmptyList
import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class TransactionsOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import TransactionsOps._

  lazy val transactor: Transactor[IO] = xa

  it should "perform queries on transactions table" in {
    check(insert)
    check(select(""))
    check(findAllByBlockId(""))
    check(countTxsNumbersByBlocksIds(NonEmptyList.fromListUnsafe(List(""))))
    check(getTxsByAddressId("", 0, 0))
    check(countTxsByAddressId(""))
    check(searchById(""))
  }
}
