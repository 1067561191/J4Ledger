package io.github.cming0420.agenticledger.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

data class LedgerEntry(
  val id: String,
  val type: LedgerEntryType = LedgerEntryType.Expense,
  val amountCents: Long,
  val channel: String,
  val category: String,
  val description: String,
  val occurredAtMillis: Long,
  val rawText: String,
  val createdAtMillis: Long,
  val transactionId: String = "",
)

enum class LedgerEntryType(val storageValue: String, val label: String) {
  Expense("EXPENSE", "消费"),
  Income("INCOME", "收入");

  companion object {
    fun fromStorageValue(value: String): LedgerEntryType =
      entries.firstOrNull { it.storageValue == value } ?: Expense
  }
}

data class AgentSettings(
  val appTitle: String = "AgenticLedger",
  val baseUrl: String = "",
  val apiKey: String = "",
  val modelName: String = "",
  val themeMode: AppThemeMode = AppThemeMode.System,
  val isImportedFromBackup: Boolean = false,
  val drawerExpanded: Boolean = true,
)

enum class AppThemeMode(val storageValue: String, val label: String) {
  System("SYSTEM", "跟随系统"),
  Light("LIGHT", "亮色"),
  Dark("DARK", "暗色");

  companion object {
    fun fromStorageValue(value: String): AppThemeMode =
      entries.firstOrNull { it.storageValue == value } ?: System
  }
}

data class LedgerFilter(
  val startDate: String = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
  val endDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
)

data class LedgerSnapshot(
  val entries: List<LedgerEntry> = emptyList(),
  val settings: AgentSettings = AgentSettings(),
)

data class BackupOptions(
  val includeEntries: Boolean,
  val includeSettings: Boolean,
  val entryFilter: LedgerFilter = LedgerFilter(),
)

fun Long.asYuanText(): String {
  val yuan = this / 100.0
  return String.format(Locale.CHINA, "%.2f", yuan)
}

fun Double.toCents(): Long = (this * 100).roundToLong()

fun LedgerEntry.signedAmountCents(): Long =
  when (type) {
    LedgerEntryType.Expense -> -amountCents
    LedgerEntryType.Income -> amountCents
  }

fun LedgerEntry.localDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
  Instant.ofEpochMilli(occurredAtMillis).atZone(zoneId).toLocalDate()

fun LedgerEntry.localDateTimeText(zoneId: ZoneId = ZoneId.systemDefault()): String =
  Instant.ofEpochMilli(occurredAtMillis)
    .atZone(zoneId)
    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
