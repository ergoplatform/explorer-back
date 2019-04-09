package org.ergoplatform.explorer.db

import io.circe.Json
import io.circe.parser._
import org.ergoplatform.explorer.db.models._

import scala.io.Source

object PreparedData extends App {

  def readArray(s: String): List[String] = s.replaceAll("\\{","").replaceAll("\\}", "").split(",").toList

  def readJson(s: String): Json = {
    val sanitizedString = s.drop(1).dropRight(1).replaceAll("'","")
    parse(sanitizedString).toOption.getOrElse(Json.Null)
  }

  def lineToHeader(s: String): Header = {
    val data = s.split(";")

    Header(
      id = data(0),
      parentId = data(1),
      version = data(2).toShort,
      height = data(3).toLong,
      nBits = data(4).toLong,
      difficulty = data(5).toLong,
      timestamp = data(6).toLong,
      stateRoot = data(7),
      adProofsRoot = data(8),
      transactionsRoot = data(9),
      extensionHash = data(10),
      minerPk = "020dbc0e4f5f57235250f840988e025e8ef54348cc6ae3f2e3c3a4cc88724295d0",
      w = "0320514b1620dedb092edefbbe8d883289caceccb2f23707058396606f482ed650",
      n = "00000000000083ae",
      d = "549147274744846704056800281002663775202262031175081146646290287367723e",
      votes = "000000",
      mainChain = data(13).contains("t")
    )
  }

  def lineToBlockInfo(s: String): BlockInfo = {
    val data = s.split(";")

    BlockInfo(
      headerId = data(0),
      timestamp = data(1).toLong,
      height = data(2).toLong,
      difficulty = data(3).toLong,
      blockSize = data(4).toLong,
      blockCoins = data(5).toLong,
      blockMiningTime = data(6).toLong,
      txsCount = data(7).toLong,
      txsSize = data(8).toLong,
      minerAddress = data(9),
      minerReward = data(10).toLong,
      minerRevenue = data(11).toLong,
      blockFee = data(12).toLong,
      blockChainTotalSize = data(13).toLong,
      totalTxsCount = data(14).toLong,
      totalCoinsIssued = data(15).toLong,
      totalMiningTime = data(16).toLong,
      totalFees = data(17).toLong,
      totalMinersReward = data(18).toLong,
      totalCoinsInTxs = data(19).toLong
    )
  }

  def lineToTx(s: String): Transaction = {
    val data = s.split(";")

    Transaction(
      id = data(0),
      headerId = data(1),
      isCoinbase = data(2).contains("t"),
      timestamp = data(3).toLong,
      size = data(4).toLong
    )
  }

  def lineToInput(s: String): Input = {
    val data = s.split(";")

    Input(
      boxId = data(0),
      txId = data(1),
      proofBytes = data(2),
      extension = readJson(data(3))
    )
  }

  def lineToOutput(s: String): Output = {
    val data = s.split(";")

    Output(
      boxId = data(0),
      txId = data(1),
      value = data(2).toLong,
      index = data(3).toInt,
      proposition = data(4),
      hash = data(5),
      assets = readJson(data(7)),
      additionalRegisters = readJson(data(6)),
      0L
    )
  }

  def lineToProof(s: String): AdProof = {
    val data = s.split(";")

    AdProof(
      headerId = data(0),
      proofBytes = data(1),
      digest = data(2)
    )
  }

  def readData[T](filename: String, f: String => T): List[T] = Source.fromResource(filename).getLines().toList.map(f)

  def readHeaders: List[Header] = readData("db_dump/headers.csv", lineToHeader)
  def readBlockInfos: List[BlockInfo] = readData("db_dump/bi.csv", lineToBlockInfo)
  def readTxs: List[Transaction] = readData("db_dump/txs.csv", lineToTx)
  def readInputs: List[Input] = readData("db_dump/inputs.csv", lineToInput)
  def readOutputs: List[Output] = readData("db_dump/outputs.csv", lineToOutput)
  def readProofs: List[AdProof] = readData("db_dump/proofs.csv", lineToProof)



  lazy val data: (List[Header], List[BlockInfo], List[Transaction], List[Input], List[Output], List[AdProof]) = {
    (readHeaders, readBlockInfos, readTxs, readInputs, readOutputs, readProofs)
  }

}
