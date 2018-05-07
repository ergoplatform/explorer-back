package org.ergoplatform.explorer.config

case class DbConfig(url: String = "jdbc:postgresql://localhost:5432/explorer",
                    user: String = "ergo",
                    ass: String = "pass")
