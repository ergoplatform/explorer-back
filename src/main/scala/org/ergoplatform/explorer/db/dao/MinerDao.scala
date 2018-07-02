package org.ergoplatform.explorer.db.dao

import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Miner

class MinerDao {

  val fields = MinerOps.fields


  def insert(m: Miner): ConnectionIO[Miner] = MinerOps.insert.withUniqueGeneratedKeys[Miner](fields: _*)(m)

  def insertMany(list: List[Miner]): ConnectionIO[List[Miner]] =
    MinerOps.insert.updateManyWithGeneratedKeys[Miner](fields: _*)(list).compile.to[List]

  def update(m: Miner): ConnectionIO[Miner] = MinerOps.update.withUniqueGeneratedKeys[Miner](fields: _*)((m, m.address))

  def delete(minerAddress: String): ConnectionIO[Unit] = MinerOps.delete(minerAddress).run(minerAddress).map(_ => Unit)

  def find(minerAddress: String): ConnectionIO[Option[Miner]] = MinerOps.find(minerAddress).option
}
