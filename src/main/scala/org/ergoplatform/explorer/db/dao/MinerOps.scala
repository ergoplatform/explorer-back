package org.ergoplatform.explorer.db.dao

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Miner

object MinerOps {

  val fields = Seq("miner_address", "miner_name")

  val fieldsString = fields.mkString(", ")
  val holdersString = fields.map(_ => "?").mkString(", ")
  val updateString = fields.map(f => s"$f = ?").mkString(", ")

  val fieldsFr = Fragment.const(fieldsString)

  val insertSql = s"INSERT INTO known_miners ($fieldsString) VALUES ($holdersString)"
  val updateByAddressSql = s"UPDATE known_miners SET $updateString WHERE miner_address = ?"

  def insert: Update[Miner] = Update[Miner](insertSql)

  def delete(minerAddress: String): Update[String] =
    Update[String](s"DELETE FROM known_miners WHERE miner_address = ?")

  def update: Update[(Miner, String)] = Update[(Miner, String)](updateByAddressSql)

  def find(minerAddress: String): Query0[Miner] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM known_miners WHERE miner_address = $minerAddress").query[Miner]

  def searchAddress(substring: String): Query0[String] =
    (fr"SELECT miner_address FROM known_miners WHERE miner_address LIKE ${"%" + substring +"%"}").query[String]
}
