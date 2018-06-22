package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.PreparedDB
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class BlockInfoOpsSpec extends FlatSpec with Matchers with BeforeAndAfterAll with IOChecker with PreparedDB {

  import BlockInfoOps._

  lazy val transactor: Transactor[IO] = xa

  it should "perform queries on blocks_info table" in {
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
}
