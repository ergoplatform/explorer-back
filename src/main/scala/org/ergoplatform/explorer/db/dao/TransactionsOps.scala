package org.ergoplatform.explorer.db.dao

import cats.data.NonEmptyList
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Transaction

object TransactionsOps {

  val fields = Seq(
    "id",
    "block_id",
    "is_coinbase",
    "ts"
  )

  val fieldsFr = Fragment.const(fields.mkString(", "))
  val insertSql = "INSERT INTO transactions (id, block_id, is_coinbase, ts) VALUES (?, ? ,? ,?)"

  def findAllByBlockId(blockId: String)(implicit c: Composite[Transaction]): Query0[Transaction] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM transactions WHERE block_id = $blockId").query[Transaction]

  def countTxsNumbersByBlocksIds(blockIds: NonEmptyList[String]): Query0[(String, Long)] =
    (fr"SELECT block_id, count(*) FROM transactions WHERE" ++
      Fragments.in(fr"block_id", blockIds) ++ fr"GROUP BY block_id").query[(String, Long)]

  def getTxsByAddressId(addressId: String, offset: Int, limit: Int)
                       (implicit c: Composite[Transaction]): Query0[Transaction] =
    fr"""
        SELECT t.id, t.block_id, t.is_coinbase, t.ts
        FROM transactions t
        WHERE EXISTS (
          SELECT 1
          FROM outputs os
          WHERE (os.tx_id = t.id AND os.hash = $addressId)
        )
        OFFSET ${offset.toLong} LIMIT ${limit.toLong};
      """.query[Transaction]

  def countTxsByAddressId(addressId: String): Query0[Long] = {
      fr"""
         SELECT COUNT(t.id)
         FROM transactions t
         WHERE EXISTS (
           SELECT 1
           FROM outputs os
           WHERE (os.tx_id = t.id AND os.hash = $addressId)
         )
         """.query[Long]
  }

  def insert: Update[Transaction] = Update[Transaction](insertSql)

  def select(id: String): Query0[Transaction] =
    fr"SELECT id, block_id, is_coinbase, ts FROM transactions WHERE id = $id".query[Transaction]

  def searchById(substring: String): Query0[String] =
    fr"SELECT id FROM transactions WHERE id LIKE ${"%" + substring + "%" }".query[String]
}
