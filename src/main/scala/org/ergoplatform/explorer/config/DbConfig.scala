package org.ergoplatform.explorer.config

import scala.io.Source
import scala.util.Try

case class DbConfig(url: String = "jdbc:postgresql://localhost:5432/explorer",
                    user: String = "ergo",
                    password: Option[String] = None,
                    passfile: Option[String] = None,
                    driverClassName: String = "org.postgresql.Driver",
                    migrateOnStart: Boolean = false,
                    servicesConnPoolSize: Int = 32,
                    grabberConnPoolSize: Int = 16) {

  def pass: String = (password orElse readfile(passfile)).getOrElse("pass")

  def readfile(filename: Option[String]): Option[String] = filename.flatMap { f =>
    Try(Source.fromFile(f, "UTF8").mkString).toOption
  }
}
