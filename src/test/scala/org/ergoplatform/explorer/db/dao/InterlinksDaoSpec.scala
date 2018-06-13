package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.utils.generators.{HeadersGen, InterlinksGenerator}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class InterlinksDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert and find data" in new {

    val dao = new InterlinksDao

    val headers = HeadersGen.generateHeaders(10)
    val interlinks = InterlinksGenerator.generateInterlinks(headers)

    val head = interlinks.head
    val tail = interlinks.tail

    noException should be thrownBy dao.insert(head._1, head._2).transact(xa).unsafeRunSync()
    noException should be thrownBy dao.insertMany(tail).transact(xa).unsafeRunSync()

    headers.foreach { h =>
      val id = h.id
      val expectedInterlinks = interlinks.filter(_._2 == id).map(_._1)
      val interlinksFromDb = dao.findAllByBlockId(id).transact(xa).unsafeRunSync().map(_.modifierId)
      expectedInterlinks should contain theSameElementsAs interlinksFromDb
    }
  }
}
