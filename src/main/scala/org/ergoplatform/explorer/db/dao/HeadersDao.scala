package org.ergoplatform.explorer.db.dao

import cats.implicits._
import doobie._, doobie.implicits._, doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Header

class HeadersDao {

  val fields: Seq[String] = HeadersOps.fields

  def insert(h: Header): ConnectionIO[Header] = {
    HeadersOps
      .insert
      .withUniqueGeneratedKeys[Header](fields: _*)(h)
  }

  def insertMany(list: List[Header]): ConnectionIO[List[Header]] = {
    HeadersOps
      .insert
      .updateManyWithGeneratedKeys[Header](fields: _*)(list)
      .compile
      .to[List]
  }

  def update(h: Header): ConnectionIO[Header] = {
    HeadersOps
      .update
      .withUniqueGeneratedKeys[Header](fields: _*)(h -> h.id)
  }

  def updateMany(list: List[Header]): ConnectionIO[List[Header]] = {
    HeadersOps
      .update
      .updateManyWithGeneratedKeys[Header](fields: _*)(list.map(h => h -> h.id))
      .compile
      .to[List]
  }

  def find(id: String): ConnectionIO[Option[Header]] = HeadersOps.select(id).option

  def findByD(d: String): ConnectionIO[Option[Header]] = HeadersOps.selectByD(d).option

  def findByParentId(parentId: String): ConnectionIO[Option[Header]] = HeadersOps.selectByParentId(parentId).option

  def get(id: String): ConnectionIO[Header] = find(id).flatMap {
    case Some(h) => h.pure[ConnectionIO]
    case None => doobie.free.connection.raiseError(
      new NoSuchElementException(s"Cannot find header with id = $id")
    )
  }

  def getByD(d: String): ConnectionIO[Header] = findByD(d).flatMap {
    case Some(h) => h.pure[ConnectionIO]
    case None => doobie.free.connection.raiseError(
      new NoSuchElementException(s"Cannot find header with d = $d")
    )
  }

  def getAtHeightRange(minH: Long, maxH: Long): ConnectionIO[List[Header]] =
    HeadersOps.selectByHeightRange(minH, maxH).to[List]

  def getLast(limit: Int = 20): ConnectionIO[List[Header]] = HeadersOps.selectLast(limit).to[List]

  def getAtHeight(height: Long): ConnectionIO[List[Header]] = HeadersOps.selectByHeight(height).to[List]

  def getHeightById(id: String): ConnectionIO[Long] = HeadersOps.selectHeight(id).option.flatMap {
    case Some(h) => h.pure[ConnectionIO]
    case None => (-1L).pure[ConnectionIO]
  }

}
