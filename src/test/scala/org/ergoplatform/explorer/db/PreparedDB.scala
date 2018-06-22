package org.ergoplatform.explorer.db

import cats.effect.IO
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, TestSuite}

trait PreparedDB extends JsonMeta { self: TestSuite with BeforeAndAfterAll =>

  val container = {
    val c = PostgreSQLContainer("postgres:latest")
    c.container.withUsername("ergo").withDatabaseName("explorer")
    c
  }

  lazy val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    container.driverClassName,
    container.jdbcUrl,
    container.username,
    container.password
  )

  override def beforeAll(): Unit = {
    container.container.start()
    val flyway = new Flyway()
    flyway.setSqlMigrationSeparator("__")
    flyway.setLocations("filesystem:sql")
    flyway.setDataSource(container.jdbcUrl, container.username, container.password)
    flyway.migrate()
  }

  override def afterAll(): Unit = {
    container.container.stop()
  }

}
