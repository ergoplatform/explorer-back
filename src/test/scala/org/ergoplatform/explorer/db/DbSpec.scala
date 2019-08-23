package org.ergoplatform.explorer.db

import java.io.PrintWriter
import java.nio.file.Files

import org.ergoplatform.explorer.config.DbConfig
import org.scalatest.{FlatSpec, Matchers}

class DbSpec extends FlatSpec with Matchers with DB {

  it should "read pass from db config correctly" in {

    //Read default value
    val dbConfig1 = DbConfig()
    credentials(dbConfig1).unsafeRunSync() shouldBe "pass"

    //Read value from DB_PASS env
    val dbConfig2 = DbConfig().copy(passOpt = Some("not_empty"))
    credentials(dbConfig2).unsafeRunSync() shouldBe "not_empty"

    //Prioritize value from DB_PASS over DB_PASS_FILE
    val dbConfig3 = dbConfig2.copy(passFilePathOpt = Some("some/file/where"))
    credentials(dbConfig3).unsafeRunSync() shouldBe "not_empty"

    //Read value from file which has been set in DB_PASS_FILE
    val path = Files.createTempFile("123", "321")
    val writer = new PrintWriter(path.toAbsolutePath.toString)
    writer.write("pass_from_file")
    writer.flush()
    writer.close()

    val dbConfig4 = DbConfig().copy(passFilePathOpt = Some(path.toAbsolutePath.toString))
    credentials(dbConfig4).unsafeRunSync() shouldBe "pass_from_file"
  }

}
