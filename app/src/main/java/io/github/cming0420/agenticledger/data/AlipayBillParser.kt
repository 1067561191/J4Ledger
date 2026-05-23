package io.github.cming0420.agenticledger.data

import java.io.InputStream
import java.nio.charset.Charset

object AlipayBillParser {

  private const val HEADER_ROW_INDEX = 22
  private const val DATA_START_ROW_INDEX = 23

  fun parse(inputStream: InputStream): List<BillRecord> {
    val bytes = inputStream.readBytes()
    val text = String(bytes, Charset.forName("GB2312"))

    val lines = text.lines()
    if (lines.size <= DATA_START_ROW_INDEX) error("文件格式错误，请确认是支付宝账单文件")

    val headerLine = lines[HEADER_ROW_INDEX]
    val headers = headerLine.split(",").map { it.trim() }

    val records = mutableListOf<BillRecord>()
    for (i in DATA_START_ROW_INDEX until lines.size) {
      val line = lines[i].trim()
      if (line.isBlank()) continue

      val cells = line.split(",").map { it.trim().replace("\t", "") }
      if (cells.size < 9) continue

      val direction = cells.getOrElse(5) { "" }
      if (direction == "不计收支") continue

      val amount = cells.getOrElse(6) { "0" }
        .replace(",", "")
        .toDoubleOrNull() ?: 0.0

      val remark = buildString {
        val orderId = cells.getOrElse(9) { "" }.trim()
        val merchantOrderId = cells.getOrElse(10) { "" }.trim()
        if (orderId.isNotBlank()) append(orderId)
        if (merchantOrderId.isNotBlank()) {
          if (isNotEmpty()) append(" ")
          append(merchantOrderId)
        }
      }

      records.add(
        BillRecord(
          transactionTime = cells.getOrElse(0) { "" },
          transactionType = cells.getOrElse(1) { "" },
          counterpart = cells.getOrElse(2) { "" },
          product = cells.getOrElse(4) { "" },
          direction = direction,
          amount = amount,
          paymentMethod = "支付宝",
          status = cells.getOrElse(8) { "" },
          transactionId = cells.getOrElse(9) { "" },
          remark = remark,
          category = cells.getOrElse(1) { "其他" }.ifBlank { "其他" },
        )
      )
    }

    return records
  }
}
