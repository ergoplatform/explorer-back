package org.ergoplatform.explorer.db.dao

import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.models.Header

object HeadersOps extends DaoOps {

  val tableName: String = "node_headers"

  val fields: Seq[String] = Seq(
    "id",
    "parent_id",
    "version",
    "height",
    "n_bits",
    "difficulty",
    "timestamp",
    "state_root",
    "ad_proofs_root",
    "transactions_root",
    "extension_hash",
    "miner_pk",
    "w",
    "n",
    "d",
    "votes",
    "main_chain"
  )

  def select(id: String): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE id = $id;").query[Header]

  def selectByD(d: String): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE d = $d;").query[Header]

  def selectByHeight(height: Long): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE height = $height").query

  def selectByHeightRange(minH: Long, maxH: Long): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE height >= $minH AND height <= $maxH").query

  def selectByParentId(parentId: String): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE parent_id = $parentId AND main_chain = TRUE")
      .query[Header]

  def selectLast(limit: Int = 20): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE main_chain = TRUE ORDER BY height DESC LIMIT ${limit.toLong}").query

  def selectHeight(id: String): Query0[Long] =
    fr"SELECT height FROM node_headers WHERE id = $id".query[Long]

  def insert: Update[Header] = Update[Header](insertSql)

  def update: Update[(Header, String)] = Update[(Header, String)](updateByIdSql)

  def count(sTs: Long, eTs: Long): Query0[Long] =
    fr"SELECT count(id) FROM node_headers WHERE (timestamp >= $sTs) AND (timestamp <= $eTs) AND main_chain = TRUE"
      .query[Long]

  def list(
    offset: Int = 0,
    limit: Int = 20,
    sortBy: String,
    sortOrder: String,
    startTs: Long,
    endTs: Long
  ): Query0[Header] =
    (
      fr"SELECT" ++ fieldsFr ++
      fr"FROM node_headers WHERE ((timestamp >= $startTs) AND (timestamp <= $endTs) AND main_chain = TRUE)" ++
      Fragment.const("ORDER BY " + sortBy + " " + sortOrder) ++
      fr"LIMIT ${limit.toLong} OFFSET ${offset.toLong};"
    ).query[Header]

  def searchById(substring: String): Query0[Header] =
    (
      fr"SELECT" ++ fieldsFr ++
      fr"FROM node_headers WHERE id LIKE ${"%" + substring + "%"}"
    ).query[Header]

}
