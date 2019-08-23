package org.ergoplatform.explorer.db

import cats.effect.{ContextShift, IO}
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, TestSuite}

import scala.concurrent.ExecutionContext.Implicits

trait PreparedDB extends JsonMeta { self: TestSuite with BeforeAndAfterAll =>

  implicit val cs: ContextShift[IO] = IO.contextShift(Implicits.global)

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
    flyway.setLocations("classpath:db")
    flyway.setDataSource(container.jdbcUrl, container.username, container.password)
    flyway.migrate()
  }

  override def afterAll(): Unit = {
    container.container.stop()
  }

}
