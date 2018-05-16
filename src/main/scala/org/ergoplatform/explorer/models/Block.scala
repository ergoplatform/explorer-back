package org.ergoplatform.explorer.models

case class Block(header: Header,
                 interlinks: List[Interlink],
                 txs: List[Transaction],
                 inputs: List[Input],
                 outputs: List[Output]
                )

object Block {

}
