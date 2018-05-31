package org.ergoplatform.explorer.config

import java.io.PrintWriter
import java.nio.file.Files

import org.scalatest.{FlatSpec, Matchers}

class DbConfigSpec extends FlatSpec with Matchers {

  it should "read pass from db config correctly" in {

    //Read default value
    val dbConfig1 = DbConfig()
    dbConfig1.pass shouldBe "pass"

    //Read value from DB_PASS env
    val dbConfig2 = DbConfig().copy(password = Some("not_empty"))
    dbConfig2.pass shouldBe "not_empty"

    //Prioritize value from DB_PASS over DB_PASS_FILE
    val dbConfig3 = dbConfig2.copy(passfile = Some("some/file/where"))
    dbConfig3.pass shouldBe "not_empty"

    //Read value from file which has been set in DB_PASS_FILE
    val path = Files.createTempFile("123", "321")
    val writer = new PrintWriter(path.toAbsolutePath.toString)
    writer.write("pass_from_file")
    writer.flush()
    writer.close()

    val dbConfig4 = DbConfig().copy(passfile = Some(path.toAbsolutePath.toString))
    dbConfig4.pass shouldBe "pass_from_file"
  }
}
