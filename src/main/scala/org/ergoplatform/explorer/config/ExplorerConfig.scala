package org.ergoplatform.explorer.config

final case class ExplorerConfig(
  db: DbConfig,
  http: HttpConfig,
  grabber: GrabberConfig,
  protocol: ProtocolConfig
)
