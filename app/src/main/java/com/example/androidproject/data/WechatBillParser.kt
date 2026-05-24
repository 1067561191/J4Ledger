package com.example.androidproject.data

import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object WechatBillParser {

  private const val HEADER_ROW_INDEX = 17
  private const val DATA_START_ROW_INDEX = 18
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

  fun parse(inputStream: InputStream): List<WechatBillRecord> {
    val workbook = XSSFWorkbook(inputStream)
    val sheet = workbook.getSheetAt(0)

    val headerRow = sheet.getRow(HEADER_ROW_INDEX)
      ?: error("无法读取表头，请确认是微信账单文件")
    val headers = (0 until headerRow.lastCellNum).map { idx ->
      headerRow.getCell(idx)?.toString()?.trim() ?: ""
    }

    val records = mutableListOf<WechatBillRecord>()
    var rowIdx = DATA_START_ROW_INDEX
    while (true) {
      val row = sheet.getRow(rowIdx) ?: break

      val cells = (0 until headers.size).map { idx ->
        getCellStringValue(row.getCell(idx))
      }
      if (cells.all { it.isBlank() }) break

      val amount = cells.getOrElse(5) { "0" }
        .replace(",", "")
        .replace("¥", "")
        .replace("￥", "")
        .toDoubleOrNull() ?: 0.0

      records.add(
        WechatBillRecord(
          transactionTime = cells.getOrElse(0) { "" },
          transactionType = cells.getOrElse(1) { "" },
          counterpart = cells.getOrElse(2) { "" },
          product = cells.getOrElse(3) { "" },
          direction = cells.getOrElse(4) { "" },
          amount = amount,
          paymentMethod = "微信",
          status = cells.getOrElse(7) { "" },
          transactionId = cells.getOrElse(8) { "" },
          remark = cells.getOrElse(10) { "" },
        )
      )
      rowIdx++
    }

    workbook.close()
    return records
  }

  private fun getCellStringValue(cell: org.apache.poi.ss.usermodel.Cell?): String {
    if (cell == null) return ""
    return when (cell.cellType) {
      CellType.NUMERIC -> {
        if (DateUtil.isCellDateFormatted(cell)) {
          dateFormat.format(cell.dateCellValue)
        } else {
          cell.numericCellValue.toString()
        }
      }
      CellType.STRING -> cell.stringCellValue.trim()
      CellType.BOOLEAN -> cell.booleanCellValue.toString()
      CellType.FORMULA -> {
        try {
          cell.stringCellValue.trim()
        } catch (_: Exception) {
          cell.toString().trim()
        }
      }
      else -> cell.toString().trim()
    }
  }
}
