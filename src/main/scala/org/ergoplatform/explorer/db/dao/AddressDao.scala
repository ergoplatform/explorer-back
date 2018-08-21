package org.ergoplatform.explorer.db.dao

import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import org.ergoplatform.explorer.db.models.AddressSummaryData

class AddressDao {
  def getAddressData(hash: String): ConnectionIO[AddressSummaryData] = OutputsOps.addressStats(hash).option.flatMap {
    case Some(a) => a.pure[ConnectionIO]
    case None => doobie.free.connection.raiseError(
      new NoSuchElementException(s"Cannot find address = $hash")
    )
  }
}
