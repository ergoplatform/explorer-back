package org.ergoplatform.explorer.db.dao

import doobie.Composite
import doobie.free.connection.ConnectionIO
import doobie.util.fragment.Fragment
import org.ergoplatform.explorer.db.models.Header

class HeadersDao extends BaseDoobieDao[String, Header] {

  override def table: String = "headers"

  override def fields: Seq[String] = Seq(
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
    "miner_address"
  )

  def whereByTs(s: Long, e: Long): Fragment = Fragment.const(
    s"WHERE (ts <= $e) AND (ts >= $s)"
  )

  def getLastN(count: Int = 20)
              (implicit e: Composite[Header]): ConnectionIO[List[Header]] = {
    val sql = selectAllFromFr ++ sortByFr("height", "DESC") ++ limitFr(count)
    sql.query[Header].stream.compile.toList
  }

  def getHeightById(id: String): ConnectionIO[Int] = {
    val sql = s"SELECT height from $table WHERE id = '$id'"
    Fragment.const(sql).query[Int].unique
  }

  def findNextBlockId(id: String): ConnectionIO[Option[String]] = {
    val sql = s"SELECT id from $table WHERE parent_id = '$id'"
    Fragment.const(sql).query[String].option
  }

  def listByDate(offset: Int = 0,
                 limit: Int = 20,
                 sortBy: String = idFieldName,
                 sortOder: String = "ASC",
                 startTs: Long,
                 endTs: Long)
                (implicit e: Composite[Header]): ConnectionIO[List[Header]] = {
    val sql = selectAllFromFr ++ whereByTs(startTs, endTs) ++
      sortByFr(sortBy, sortOder) ++ limitFr(limit) ++ offsetFr(offset)
    sql.query[Header].stream.compile.toList
  }

}
