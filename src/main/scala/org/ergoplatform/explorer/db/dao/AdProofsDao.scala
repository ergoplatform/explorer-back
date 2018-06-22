package org.ergoplatform.explorer.db.dao

import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.AdProof

class AdProofsDao {

  def find(headerId: String): ConnectionIO[Option[AdProof]] = AdProofsOps.select(headerId).option

}
