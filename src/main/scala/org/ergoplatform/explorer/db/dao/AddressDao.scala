package org.ergoplatform.explorer.db.dao

import doobie.free.connection.ConnectionIO
import org.ergoplatform.explorer.db.models.AddressSummaryData

class AddressDao {
  def getAddressData(hash: String): ConnectionIO[AddressSummaryData] = OutputsOps.addressStats(hash).unique
}
