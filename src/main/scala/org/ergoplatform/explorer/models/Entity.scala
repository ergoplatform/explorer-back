package org.ergoplatform.explorer.models

trait Entity[ID] {
  val id: ID
  val idFieldName = "id"
}
