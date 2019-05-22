package org.ergoplatform.explorer.db.models

case class AddressPortfolio(address: String,
                            txsCount: Long,
                            spent: BigInt,
                            balance: Long,
                            tokensBalance: Map[String, Long])