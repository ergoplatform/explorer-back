package org.ergoplatform.explorer.db.dao

import doobie._, doobie.implicits._, doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Header

object HeadersOps {

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
    "equihash_solutions",
    "interlinks",
    "size",
    "main_chain"
  )

  val fieldsString = fields.mkString(", ")
  val holdersString = fields.map(_ => "?").mkString(", ")
  val updateString = fields.map(f => s"$f = ?").mkString(", ")

  val fieldsFr = Fragment.const(fields.mkString(", "))
  val insertSql = s"INSERT INTO node_headers($fieldsString) VALUES ($holdersString)"
  val updateByIdSql = s"UPDATE node_headers SET $updateString WHERE id = ?"

  def select(id: String): Query0[Header] = (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE id = $id;").query[Header]

  def selectByParentId(parentId: String): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE parent_id = $parentId").query[Header]

  def selectLast(limit: Int = 20): Query0[Header] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_headers WHERE main_chain = TRUE ORDER BY height DESC LIMIT ${limit.toLong}").query

  def selectHeight(id: String): Query0[Long] = fr"SELECT height FROM node_headers WHERE id = $id".query[Long]

  def insert: Update[Header] = Update[Header](insertSql)

  def update: Update[(Header, String)] = Update[(Header, String)](updateByIdSql)

  def count(sTs: Long, eTs: Long): Query0[Long] =
    fr"SELECT count(id) FROM node_headers WHERE (timestamp >= $sTs) AND (timestamp <= $eTs) AND main_chain == TRUE"
      .query[Long]

  def list(offset: Int = 0,
           limit: Int = 20,
           sortBy: String,
           sortOrder: String,
           startTs: Long,
           endTs: Long): Query0[Header] = (
    fr"SELECT" ++ fieldsFr ++
      fr"FROM node_headers WHERE ((timestamp >= $startTs) AND (timestamp <= $endTs) AND main_chain = TRUE)" ++
      Fragment.const("ORDER BY " + sortBy + " " + sortOrder) ++
      fr"LIMIT ${limit.toLong} OFFSET ${offset.toLong};"
    ).query[Header]

  def searchById(substring: String): Query0[Header] = (
    fr"SELECT" ++ fieldsFr ++
    fr"FROM node_headers WHERE id LIKE ${"%" + substring + "%"}"
  ).query[Header]

}
