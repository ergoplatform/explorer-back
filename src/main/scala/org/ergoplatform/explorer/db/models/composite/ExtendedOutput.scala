package org.ergoplatform.explorer.db.models.composite

import org.ergoplatform.explorer.db.models.Output

final case class ExtendedOutput(output: Output, spentByOpt: Option[String], mainChain: Boolean)
