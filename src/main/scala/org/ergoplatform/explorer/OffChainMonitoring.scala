package org.ergoplatform.explorer

import org.ergoplatform.explorer.persistence.OffChainPersistence

trait OffChainMonitoring {

  lazy val offChainStorage = new OffChainPersistence

}
