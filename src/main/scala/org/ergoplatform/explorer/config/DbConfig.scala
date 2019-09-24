package org.ergoplatform.explorer.config

final case class DbConfig(
  url: String = "jdbc:postgresql://localhost:5432/explorer",
  user: String = "ergo",
  pass: Option[String] = None,
  passFilePath: Option[String] = None,
  driverClassName: String = "org.postgresql.Driver",
  migrateOnStart: Boolean = false,
  servicesConnPoolSize: Int = 32,
  grabberConnPoolSize: Int = 8
)
