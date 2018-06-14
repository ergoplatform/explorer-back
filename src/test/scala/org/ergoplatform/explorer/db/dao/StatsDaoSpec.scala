package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.utils.generators.StatsGenerator
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class StatsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert, find" in new {

    val dao = new StatsDao
    val dao1 = new StatsDao

    val stats = StatsGenerator.generateStats(20).zipWithIndex.map { case (s, i) => s.copy(id = i.toLong) }

    val head = stats.head
    val tail = stats.tail

    dao.insert(head).transact(xa).unsafeRunSync() shouldBe head
    dao.insertMany(tail).transact(xa).unsafeRunSync() should contain theSameElementsAs tail

    dao.findLast.transact(xa).unsafeRunSync() shouldBe Some(stats.maxBy(_.timestamp))

    dao.difficultiesSumSince(System.currentTimeMillis()).transact(xa).unsafeRunSync() shouldBe 0L
  }
}
