package org.ergoplatform.explorer.db.dao

import doobie.free.connection.ConnectionIO
import org.ergoplatform.explorer.db.models.AddressPortfolio

class AddressDao extends OutputsDao {

  def getAddressPortfolio(address: String): ConnectionIO[AddressPortfolio] =
    findAllByAddress(address)
      .map { outputs =>
        val (spent, unspent) = outputs
          .filter(_.mainChain)
          .partition(_.spentTxId.isDefined)
        val txsQty = outputs
          .map(_.output.txId)
          .distinct
          .size
        val spentBalance = spent
          .map(_.output.value)
          .sum
        val balance = unspent
          .map(_.output.value)
          .sum
        val tokensBalance = unspent
          .flatMap(_.output.encodedAssets.toSeq)
          .foldLeft(Map.empty[String, Long]) {
            case (acc, (assetId, assetAmt)) =>
              acc.updated(assetId, acc.getOrElse(assetId, 0L) + assetAmt)
          }
        AddressPortfolio(address, txsQty, spentBalance, balance, tokensBalance)
      }

}
