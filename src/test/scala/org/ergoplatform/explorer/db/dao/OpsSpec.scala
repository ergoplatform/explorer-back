package org.ergoplatform.explorer.db.dao

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

/**
  * Better use one spec for the whole ops spec, cause we are running db in docker per file,
  * so it should reduce tests time.
  */
class OpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  lazy val transactor: Transactor[IO] = xa

  it should "check all ops in header ops" in {
    import HeadersOps._

    check(select(""))
    check(selectByParentId(""))
    check(selectLast(10))
    check(selectHeight(""))
    check(list(0, 20, "id", "ASC", 0L, 100L))
    check(insert)
    check(update)
    check(count(0L, 0L))
    check(searchById(""))
  }

  it should "check all ops in tx ops" in {
    import TransactionsOps._

    check(insert)
    check(select(""))
    check(findAllByBlockId(""))
    check(countTxsNumbersByBlocksIds(NonEmptyList.fromListUnsafe(List(""))))
    check(getTxsByAddressId("", 0, 0))
    check(countTxsByAddressId(""))
    check(searchById(""))
  }

  it should "check all block info ops" in {
    import BlockInfoOps._

    check(select(""))
    check(select(List("")))
    check(findLast(100))
    check(circulatingSupplySince(0L))
    check(difficultiesSumSince(0L))
    check(totalCoinsGroupedByDay(10))
    check(avgBlockSizeGroupedByDay(10))
    check(avgTxsGroupedByDay(10))
    check(totalBlockchainSizeGroupedByDay(10))
    check(avgDifficultyGroupedByDay(10))
    check(sumDifficultyGroupedByDay(10))
    check(minerRevenueGroupedByDay(10))
    check(insert)
    check(deleteAll)
  }

  it should "check all inputs ops" in {
    import InputsOps._

    check(insert)
    check(findAllByTxId(""))
    check(findAllByTxsId(NonEmptyList.fromListUnsafe(List(""))))
    check(findAllByTxIdWithValue(""))
    check(findAllByTxsIdWithValue(NonEmptyList.fromListUnsafe(List(""))))
  }

  it should "check all ops in output ops" in {
    import OutputsOps._

    check(insert)
    check(findAllByTxId(""))
    check(findAllByTxsId(NonEmptyList.fromListUnsafe(List(""))))
    check(findByHash(""))
    check(searchByHash(""))
    check(sumOfAllUnspentOutputs)
    check(findAllByTxIdWithSpent(""))
    check(findAllByTxsIdWithSpent(NonEmptyList.fromListUnsafe(List(""))))
    check(estimatedOutputs)
  }

  it should "check all ops in miner ops" in {
    import MinerOps._

    check(insert)
    check(update)
    check(delete(""))
    check(find(""))
  }

  it should "check all ops for block list ops" in {
    import BlockListOps._

    count(0L, 0L)
    list(0,0, "", "", 0L, 0L)
    searchById("")
  }

}
