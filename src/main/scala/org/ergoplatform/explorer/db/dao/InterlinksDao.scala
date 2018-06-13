package org.ergoplatform.explorer.db.dao

import cats.data._, cats.implicits._
import doobie._, doobie.implicits._, doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Interlink

class InterlinksDao {

  val fields = InterlinksOps.fields

  def insert(modifierId: String, blockId: String): ConnectionIO[Interlink] =
    InterlinksOps.insertTupled.withUniqueGeneratedKeys[Interlink](fields: _*)((modifierId, blockId))

  def insertMany(list: List[(String, String)]): ConnectionIO[List[Interlink]] =
    InterlinksOps.insertTupled.updateManyWithGeneratedKeys[Interlink](fields: _*)(list).compile.to[List]

  def findAllByBlockId(blockId: String): ConnectionIO[List[Interlink]] =
    InterlinksOps.findAllByBlockId(blockId).to[List]

}
