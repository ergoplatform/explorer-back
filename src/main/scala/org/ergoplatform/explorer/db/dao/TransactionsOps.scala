package org.ergoplatform.explorer.db.dao

import cats.data.NonEmptyList
import doobie.implicits._
import doobie.{Fragments, Read, _}
import org.ergoplatform.explorer.db.models.Transaction

object TransactionsOps {

  val fields: Seq[String] = Seq(
    "id",
    "header_id",
    "coinbase",
    "timestamp",
    "size"
  )

  val fieldsString: String = fields.mkString(", ")
  val holdersString: String = fields.map(_ => "?").mkString(", ")
  val fieldsFr: Fragment = Fragment.const(fieldsString)
  val insertSql = s"INSERT INTO node_transactions ($fieldsString) VALUES ($holdersString)"

  def findAllByBlockId(blockId: String)(implicit r: Read[Transaction]): Query0[Transaction] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_transactions WHERE header_id = $blockId")
      .query[Transaction]

  def countTxsNumbersByBlocksIds(blockIds: NonEmptyList[String]): Query0[(String, Long)] =
    (fr"SELECT header_id, count(*) FROM node_transactions WHERE" ++
    Fragments.in(fr"header_id", blockIds) ++ fr"GROUP BY header_id").query[(String, Long)]

  def getTxsByAddressId(addressId: String, offset: Int, limit: Int)(
    implicit r: Read[Transaction]
  ): Query0[Transaction] =
    fr"""
        SELECT t.id, t.header_id, t.coinbase, t.timestamp, t.size
        FROM node_transactions t LEFT JOIN node_headers h ON h.id  = t.header_id
        WHERE EXISTS (
          SELECT 1
          FROM node_outputs os
          FULL JOIN node_inputs i ON i.box_id = os.box_id
          WHERE (os.tx_id = t.id AND os.address = $addressId)
          OR (i.box_id = os.box_id AND i.tx_id = t.id AND os.address = $addressId)
        ) AND h.main_chain = TRUE
        ORDER BY t.timestamp DESC
        OFFSET ${offset.toLong} LIMIT ${limit.toLong}
      """.query[Transaction]

  def countTxsByAddressId(addressId: String): Query0[Long] = {
    fr"""
         SELECT COUNT(t.id)
         FROM node_transactions t LEFT JOIN node_headers h ON h.id  = t.header_id
         WHERE EXISTS (
           SELECT 1
           FROM node_outputs os
           FULL JOIN node_inputs i ON i.box_id = os.box_id
           WHERE (os.tx_id = t.id AND os.address = $addressId)
           OR (i.box_id = os.box_id AND i.tx_id = t.id AND os.address = $addressId)
         ) AND h.main_chain = TRUE
         """.query[Long]
  }

  def insert: Update[Transaction] = Update[Transaction](insertSql)

  def select(id: String): Query0[Transaction] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_transactions WHERE id = $id").query[Transaction]

  def searchById(substring: String): Query0[String] =
    fr"SELECT id FROM node_transactions WHERE id LIKE ${"%" + substring + "%"}".query[String]

  def txsSince(ts: Long): Query0[Long] =
    fr"SELECT count(*) FROM node_transactions WHERE timestamp >= $ts".query[Long]

  def txsHeight(ids: NonEmptyList[String]): Query0[(String, Long)] =
    (fr"SELECT t.id, h.height FROM node_transactions t LEFT JOIN node_headers h ON t.header_id = h.id WHERE" ++
    Fragments.in(fr"t.id", ids)).query[(String, Long)]
}
