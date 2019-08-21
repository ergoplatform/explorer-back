package org.ergoplatform.explorer.db.dao

import cats.data.NonEmptyList
import doobie._
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.query.Query0
import org.ergoplatform.explorer.db.models.BlockInfo

object BlockInfoOps {

  type SingleDataType = (Long, Long, String)

  val fields: Seq[String] = Seq(
    "header_id",
    "timestamp",
    "height",
    "difficulty",
    "block_size",
    "block_coins",
    "block_mining_time",
    "txs_count",
    "txs_size",
    "miner_address",
    "miner_reward",
    "miner_revenue",
    "block_fee",
    "block_chain_total_size",
    "total_txs_count",
    "total_coins_issued",
    "total_mining_time",
    "total_fees",
    "total_miners_reward",
    "total_coins_in_txs"
  )

  val fieldsString: String = fields.mkString(", ")
  val holdersString: String = fields.map(_ => "?").mkString(", ")
  val insertSql = s"INSERT INTO blocks_info ($fieldsString) VALUES ($holdersString)"

  val fieldsFr: Fragment = Fragment.const(fieldsString)

  def insert: Update[BlockInfo] = Update[BlockInfo](insertSql)

  def select(headerId: String): Query0[BlockInfo] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM blocks_info WHERE header_id = $headerId").query[BlockInfo]

  def select(headerIds: List[String]): Query0[BlockInfo] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM blocks_info WHERE" ++
    Fragments.in(fr"header_id", NonEmptyList.fromListUnsafe(headerIds))).query[BlockInfo]

  def findLast(cnt: Int = 10): Query0[BlockInfo] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM blocks_info ORDER BY height DESC LIMIT ${cnt.toLong}")
      .query[BlockInfo]

  def findSince(ts: Long): Query0[BlockInfo] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM blocks_info WHERE timestamp >= $ts").query[BlockInfo]

  def difficultiesSumSince(ts: Long): Query0[Long] = {
    fr"SELECT COALESCE(CAST(SUM(difficulty) as BIGINT), 0) FROM blocks_info WHERE timestamp >= $ts"
      .query[Long]
  }

  def circulatingSupplySince(ts: Long): Query0[Long] = {
    (fr"SELECT COALESCE(CAST(SUM(o.value) as BIGINT), 0) " ++
    fr"FROM node_transactions t RIGHT JOIN node_outputs o ON t.id = o.tx_id WHERE t.timestamp >= $ts")
      .query[Long]
  }

  def totalCoinsGroupedByDay(lastDays: Int): Query0[SingleDataType] = {
    val selectStr = "min(timestamp) as t, CAST(max(total_coins_issued) as BIGINT)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def avgBlockSizeGroupedByDay(lastDays: Int): Query0[SingleDataType] = {
    val selectStr = "min(timestamp) as t, CAST(avg(block_size) as BIGINT)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def avgTxsGroupedByDay(lastDays: Int): Query0[SingleDataType] = {
    val selectStr = "min(timestamp) as t, CAST(avg(txs_count) as BIGINT)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def sumTxsGroupedByDay(lastDays: Int): Query0[SingleDataType] = {
    val selectStr = "min(timestamp) as t, CAST(sum(txs_count) as BIGINT)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def totalBlockchainSizeGroupedByDay(lastDays: Int): Query0[SingleDataType] = {
    val selectStr = "min(timestamp) as t, CAST(max(block_chain_total_size) as BIGINT)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def avgDifficultyGroupedByDay(lastDays: Int): Query0[SingleDataType] = {
    val selectStr = "min(timestamp) as t, CAST(avg(difficulty) as BIGINT)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def sumDifficultyGroupedByDay(lastDays: Int): Query0[SingleDataType] = {
    val selectStr = "min(timestamp) as t, CAST(sum(difficulty) as BIGINT)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def minerRevenueGroupedByDay(lastDays: Int): Query0[SingleDataType] = {
    val selectStr = "min(timestamp) as t, CAST(sum(miner_revenue) as BIGINT)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def groupedByDayStatsPair(d: Int, selectStr: String): Query0[SingleDataType] = {
    val sql = selectByDay(d, selectStr)
    sql.query[SingleDataType]
  }

  def selectByDay(limitDaysBack: Int, selectStr: String): Fragment = {
    import scala.concurrent.duration._

    val whereFragment = if (limitDaysBack <= 0) {
      Fragment.empty
    } else {
      val ms = System.currentTimeMillis - limitDaysBack.days.toMillis
      Fragment.const(
        s"WHERE (timestamp >= $ms AND EXISTS(SELECT 1 FROM node_headers h WHERE h.main_chain = TRUE))"
      )
    }

    Fragment.const(
      s"SELECT $selectStr, TO_CHAR(TO_TIMESTAMP(timestamp / 1000), 'DD/MM/YYYY') as date " +
      s"FROM blocks_info"
    ) ++ whereFragment ++ Fragment.const("GROUP BY date ORDER BY t ASC")
  }

}
