package org.ergoplatform.explorer

import cats.effect.IO
import cats._
import cats.data._
import cats.implicits._
import com.typesafe.scalalogging.Logger
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.dao.HeadersDao
import org.ergoplatform.explorer.models.Header
import org.scalacheck.{Arbitrary, Gen}
import pureconfig.loadConfigOrThrow
import scorex.crypto.encode.Base16

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

object DbTester extends App {
  val logger = Logger("db")
  val cfg = loadConfigOrThrow[ExplorerConfig]
  logger.debug(cfg.toString)

  val xa = Transactor.fromDriverManager[IO](cfg.db.driverClassName, cfg.db.url, cfg.db.user, cfg.db.pass)
  val y = xa.yolo

  import y._

  val dao = new HeadersDao

  def headerGen(parentId: String, height: Int): Gen[Header] = for {
    id <- Gen.listOfN(32, Arbitrary.arbByte.arbitrary).map(l => Base16.encode(l.toArray))
    pId = parentId
    version = 1: Short
    h = height
    nBits <- Arbitrary.arbLong.arbitrary
    nonce <- Arbitrary.arbLong.arbitrary
    bz <- Arbitrary.arbLong.arbitrary
  } yield Header(id, pId, version, h, "", "", "", System.currentTimeMillis(), nBits, nonce,bz)

  val rootParentId = Base16.encode(Array.fill(32)(1: Byte))

  val blocks: ArrayBuffer[Header] = new scala.collection.mutable.ArrayBuffer[Header]()



  (0 until 50).foldLeft(rootParentId){ case (pId, h) =>
    val b = headerGen(pId, h).sample.get
    dao.insert(b).quick.unsafeRunSync()
    blocks += b
    b.id
  }

  val b1 = blocks.head
  val b1_1 = dao.get(b1.id).transact(xa).unsafeRunSync()

  println(b1)
  println(b1_1)

  dao.count.quick.unsafeRunSync()
  dao.list(0, 10).quick.unsafeRunSync()
  dao.list(10, 10).quick.unsafeRunSync()

  dao.updateById(b1.id, b1.copy(version = 2: Short)).quick.unsafeRunSync()
  dao.get(b1.id).quick.unsafeRunSync()
  dao.update(b1.copy(version = 3: Short)).quick.unsafeRunSync()
  dao.get(b1.id).quick.unsafeRunSync()

  dao.getLastN("height", 5).quick.unsafeRunSync()

  sql"DELETE FROM headers".update.quick.unsafeRunSync()

  dao.find("1").quick.unsafeRunSync()
  Try(dao.get("1").quick.unsafeRunSync())


  //dao.list(0, 100).q
}
