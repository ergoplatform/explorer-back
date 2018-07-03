package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import scorex.crypto.encode.Base16

import scala.util.Random

class HeadersDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert,update,select from db" in {

    val dao = new HeadersDao
    val headers = PreparedData.readHeaders

    val head = headers.head
    val tail = headers.tail

    val wrongId = Base16.encode(Array.fill(33)(1: Byte))

    dao.insert(head).transact(xa).unsafeRunSync() shouldBe head
    dao.insertMany(tail).transact(xa).unsafeRunSync() shouldBe tail

    dao.getHeightById(head.id).transact(xa).unsafeRunSync() shouldBe head.height
    dao.getByParentId(head.id).transact(xa).unsafeRunSync() shouldBe tail.head

    val rnd1 = Random.shuffle(headers).head
    dao.getHeightById(rnd1.id).transact(xa).unsafeRunSync() shouldBe rnd1.height
    dao.getByParentId(rnd1.parentId).transact(xa).unsafeRunSync() shouldBe rnd1

    dao.find(wrongId).transact(xa).unsafeRunSync() shouldBe None
    dao.findByParentId(wrongId).transact(xa).unsafeRunSync() shouldBe None
    dao.find(head.id).transact(xa).unsafeRunSync() shouldBe Some(head)

    the[NoSuchElementException] thrownBy dao.get(wrongId).transact(xa).unsafeRunSync()
    the[NoSuchElementException] thrownBy dao.getByParentId(wrongId).transact(xa).unsafeRunSync()
    dao.get(head.id).transact(xa).unsafeRunSync() shouldBe head
  }
}
