package org.ergoplatform.explorer.config

case class DbConfig(url: String = "jdbc:postgresql://localhost:5432/explorer",
                    user: String = "ergo",
                    passOpt: Option[String] = None,
                    passFilePathOpt: Option[String] = None,
                    driverClassName: String = "org.postgresql.Driver",
                    migrateOnStart: Boolean = false,
                    servicesConnPoolSize: Int = 32,
                    grabberConnPoolSize: Int = 16)
