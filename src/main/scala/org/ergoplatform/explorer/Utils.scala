package org.ergoplatform.explorer

import org.ergoplatform.{ErgoAddress, ErgoAddressEncoder}
import scorex.util.encode.Base16
import sigmastate.Values.ErgoTree
import sigmastate.serialization.{ErgoTreeSerializer, SigmaSerializer}

import scala.util.Try

object Utils {

  private val treeSerializer: ErgoTreeSerializer = new ErgoTreeSerializer

  @inline def ergoTreeToAddress(
    ergoTree: String
  )(implicit enc: ErgoAddressEncoder): Try[ErgoAddress] =
    Base16.decode(ergoTree).flatMap { bytes =>
      enc.fromProposition(treeSerializer.deserializeErgoTree(bytes).proposition)
    }

  @inline def addressToErgoTree(
    address: String
  )(implicit enc: ErgoAddressEncoder): Try[ErgoTree] =
    enc.fromString(address).map(_.script)

  @inline def ergoTreeTemplateBytes(ergoTree: String): String =
    Base16.encode(
      treeSerializer
        .deserializeHeaderWithTreeBytes(
          SigmaSerializer.startReader(Base16.decode(ergoTree).get)
        )
        ._3
    )
}
