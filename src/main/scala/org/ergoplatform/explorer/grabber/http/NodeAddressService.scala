package org.ergoplatform.explorer.grabber.http

case class NodeAddressService(nodeAddress: String) {
  val infoUri = s"$nodeAddress/info"
  def idsAtHeightUri(height: Long): String = s"$nodeAddress/blocks/at/$height"
  def fullBlockUri(id: String): String = s"$nodeAddress/blocks/$id"
}
