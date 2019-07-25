package org.ergoplatform.explorer.persistence

import java.util.concurrent.ConcurrentHashMap

import org.ergoplatform.explorer.grabber.protocol.ApiTransaction

import scala.collection.JavaConverters._

/** Shared mutable in-memory storage for off-chain transactions */
class OffChainPersistence {

  private val store = new ConcurrentHashMap[String, ApiTransaction]()

  def put(txs: List[ApiTransaction]): Unit = store.putAll(txs.map(x => x.id -> x).toMap.asJava)

  def clear(): Unit = store.clear()

  def getTx(id: String): Option[ApiTransaction] = Option(store.get(id))

  def getAll: List[ApiTransaction] = store.values().asScala.toList

}
