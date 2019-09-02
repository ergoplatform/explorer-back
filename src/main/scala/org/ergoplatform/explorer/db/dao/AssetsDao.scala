package org.ergoplatform.explorer.db.dao

import doobie.free.connection.ConnectionIO
import org.ergoplatform.explorer.db.models.Asset

class AssetsDao {

	def getByBoxId(boxId: String): ConnectionIO[List[Asset]] =
		AssetsOps.getByBoxId(boxId).to[List]

}
