package org.ergoplatform.explorer.grabber.db

import org.ergoplatform.explorer.db.dao.{DaoOps, TransactionsOps}

object TransactionWriter  extends BasicWriter {

  type ToInsert = (String, String, Boolean, Long, Long)

  val ops: DaoOps = TransactionsOps

}
