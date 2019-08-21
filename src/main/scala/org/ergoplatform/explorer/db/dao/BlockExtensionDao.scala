package org.ergoplatform.explorer.db.dao

import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.BlockExtension

class BlockExtensionDao extends JsonMeta {

  def find(headerId: String): ConnectionIO[Option[BlockExtension]] =
    BlockExtensionOps.select(headerId).option

  def getByHeaderId(id: String): ConnectionIO[BlockExtension] =
    find(id).flatMap {
      case Some(h) => h.pure[ConnectionIO]
      case None =>
        doobie.free.connection.raiseError(
          new NoSuchElementException(s"Cannot find extension with header_id = $id")
        )
    }

  def insert(bi: BlockExtension): ConnectionIO[BlockExtension] =
    BlockExtensionOps.insert
      .withUniqueGeneratedKeys[BlockExtension](BlockExtensionOps.fields: _*)(bi)

  def insertMany(list: List[BlockExtension]): ConnectionIO[List[BlockExtension]] =
    BlockExtensionOps.insert
      .updateManyWithGeneratedKeys[BlockExtension](BlockExtensionOps.fields: _*)(list)
      .compile
      .to[List]

}
