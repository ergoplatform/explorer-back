package org.ergoplatform.explorer.db.dao

import doobie._, doobie.implicits._, doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Header

object HeadersOps {

  val fields: Seq[String] = Seq(
    "id",
    "parent_id",
    "version",
    "height",
    "ad_proofs_root",
    "state_root",
    "transactions_root",
    "ts",
    "n_bits",
    "extension_hash",
    "block_size",
    "equihash_solution",
    "ad_proofs",
    "tx_count",
    "miner_name",
    "miner_address",
    "miner_reward",
    "fee",
    "txs_size"
  )

  val fieldsFr = Fragment.const(fields.mkString(", "))
  val insertSql = s"INSERT INTO headers ${fields.mkString("(", ", ", ")")} " +
    s"VALUES ${fields.map(_ => "?").mkString("(", ", ", ")")}"
  val updateByIdSql = s"UPDATE headers SET ${fields.map(f => s"$f = ?").mkString(", ")} WHERE id = ?"

  def headerToFr(h: Header): Fragment =
    fr"${h.id}, ${h.parentId}, ${h.version}, ${h.height}, ${h.adProofsRoot}, ${h.stateRoot}, ${h.transactionsRoot}," ++
      fr" ${h.timestamp}, ${h.nBits}, ${h.extensionHash}, ${h.blockSize}, ${h.equihashSolution}, ${h.adProofs}," ++
      fr"${h.txCount}, ${h.minerName}, ${h.minerAddress}"

  def select(id: String): Query0[Header] = (fr"SELECT" ++ fieldsFr ++ fr"FROM headers WHERE id = $id;").query[Header]

  def selectByParentId(parentId: String): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM headers where parent_id = $parentId").query[Header]

  def selectLast(limit: Int = 20): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM headers ORDER BY height DESC LIMIT ${limit.toLong}").query

  def selectHeight(id: String): Query0[Int] = fr"SELECT height FROM headers WHERE id = $id".query[Int]

  def insert: Update[Header] = Update[Header](insertSql)

  def update: Update[(Header, String)] = Update[(Header, String)](updateByIdSql)

  def count(sTs: Long, eTs: Long): Query0[Long] =
    fr"SELECT count(id) FROM headers  WHERE (ts >= $sTs) AND (ts <= $eTs)".query[Long]

  def list(offset: Int = 0,
           limit: Int = 20,
           sortBy: String,
           sortOrder: String,
           startTs: Long,
           endTs: Long): Query0[Header] = (
    fr"SELECT" ++ fieldsFr ++
    fr"FROM headers WHERE (ts >= $startTs) AND (ts <= $endTs)" ++
    Fragment.const("ORDER BY " + sortBy + " " + sortOrder) ++
    fr"LIMIT ${limit.toLong} OFFSET ${offset.toLong};"
    ).query[Header]
}
