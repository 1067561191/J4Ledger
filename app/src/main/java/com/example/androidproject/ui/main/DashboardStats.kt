package com.example.androidproject.ui.main

import com.example.androidproject.data.LedgerEntry
import com.example.androidproject.data.LedgerEntryType
import com.example.androidproject.data.localDate
import java.time.LocalDate

data class DashboardStats(
  val dailySeries: List<DailyAmountPoint> = emptyList(),
  val expenseByCategory: List<AmountBreakdown> = emptyList(),
  val incomeByCategory: List<AmountBreakdown> = emptyList(),
  val channelBreakdown: List<ChannelAmountBreakdown> = emptyList(),
  val incomeExpenseBreakdown: List<AmountBreakdown> = emptyList(),
) {
  val hasAnyEntries: Boolean =
    dailySeries.any { it.expenseCents > 0L || it.incomeCents > 0L }
}

data class DailyAmountPoint(
  val date: LocalDate,
  val expenseCents: Long,
  val incomeCents: Long,
)

data class AmountBreakdown(
  val label: String,
  val amountCents: Long,
)

data class ChannelAmountBreakdown(
  val channel: String,
  val expenseCents: Long,
  val incomeCents: Long,
) {
  val totalCents: Long = expenseCents + incomeCents
}

fun buildDashboardStats(
  entries: List<LedgerEntry>,
  filter: com.example.androidproject.data.LedgerFilter,
): DashboardStats {
  val start = runCatching { LocalDate.parse(filter.startDate) }.getOrNull()
  val end = runCatching { LocalDate.parse(filter.endDate) }.getOrNull()
  val dailySeries =
    if (start != null && end != null && !start.isAfter(end)) {
      buildDailySeries(entries, start, end)
    } else {
      buildDailySeries(entries)
    }
  val expenseByCategory = entries.breakdownByCategory(LedgerEntryType.Expense)
  val incomeByCategory = entries.breakdownByCategory(LedgerEntryType.Income)
  return DashboardStats(
    dailySeries = dailySeries,
    expenseByCategory = expenseByCategory,
    incomeByCategory = incomeByCategory,
    channelBreakdown = entries.breakdownByChannel(),
    incomeExpenseBreakdown =
      listOf(
        AmountBreakdown("收入", entries.totalByType(LedgerEntryType.Income)),
        AmountBreakdown("支出", entries.totalByType(LedgerEntryType.Expense)),
      ).filter { it.amountCents > 0L },
  )
}

private fun buildDailySeries(entries: List<LedgerEntry>, start: LocalDate, end: LocalDate): List<DailyAmountPoint> {
  val byDate = entries.groupBy { it.localDate() }
  val days = generateSequence(start) { date -> date.plusDays(1).takeIf { !it.isAfter(end) } }
  return days.map { date ->
    val dayEntries = byDate[date].orEmpty()
    DailyAmountPoint(
      date = date,
      expenseCents = dayEntries.totalByType(LedgerEntryType.Expense),
      incomeCents = dayEntries.totalByType(LedgerEntryType.Income),
    )
  }.toList()
}

private fun buildDailySeries(entries: List<LedgerEntry>): List<DailyAmountPoint> {
  if (entries.isEmpty()) return emptyList()
  val dates = entries.map { it.localDate() }
  return buildDailySeries(entries, dates.minOrNull() ?: return emptyList(), dates.maxOrNull() ?: return emptyList())
}

private fun List<LedgerEntry>.breakdownByCategory(type: LedgerEntryType): List<AmountBreakdown> =
  filter { it.type == type }
    .groupBy { it.category.ifBlank { "其他" } }
    .map { (category, entries) -> AmountBreakdown(category, entries.sumOf { it.amountCents }) }
    .filter { it.amountCents > 0L }
    .sortedByDescending { it.amountCents }

private fun List<LedgerEntry>.breakdownByChannel(): List<ChannelAmountBreakdown> {
  val grouped =
    groupBy { it.channel.ifBlank { "其他" } }
      .map { (channel, entries) ->
        ChannelAmountBreakdown(
          channel = channel,
          expenseCents = entries.totalByType(LedgerEntryType.Expense),
          incomeCents = entries.totalByType(LedgerEntryType.Income),
        )
      }
      .filter { it.totalCents > 0L }
      .sortedByDescending { it.totalCents }
  if (grouped.size <= MAX_CHANNEL_BUCKETS) return grouped
  val top = grouped.take(MAX_CHANNEL_BUCKETS - 1)
  val rest = grouped.drop(MAX_CHANNEL_BUCKETS - 1)
  return top +
    ChannelAmountBreakdown(
      channel = "其他渠道",
      expenseCents = rest.sumOf { it.expenseCents },
      incomeCents = rest.sumOf { it.incomeCents },
    )
}

private fun List<LedgerEntry>.totalByType(type: LedgerEntryType): Long =
  filter { it.type == type }.sumOf { it.amountCents }

private const val MAX_CHANNEL_BUCKETS = 8
