package com.example.androidproject.ui.main

import com.example.androidproject.data.LedgerEntry
import com.example.androidproject.data.LedgerEntryType
import com.example.androidproject.data.LedgerFilter
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test

class DashboardStatsTest {
  @Test
  fun dailySeries_fillsMissingDatesAndSplitsIncomeExpense() {
    val stats =
      buildDashboardStats(
        entries =
          listOf(
            entry("1", LedgerEntryType.Expense, 1200, "微信", "餐饮", "2026-05-01"),
            entry("2", LedgerEntryType.Income, 5000, "微信", "工资", "2026-05-01"),
            entry("3", LedgerEntryType.Expense, 900, "现金", "交通", "2026-05-03"),
          ),
        filter = LedgerFilter("2026-05-01", "2026-05-03"),
      )

    assertEquals(3, stats.dailySeries.size)
    assertEquals(LocalDate.parse("2026-05-02"), stats.dailySeries[1].date)
    assertEquals(0L, stats.dailySeries[1].expenseCents)
    assertEquals(0L, stats.dailySeries[1].incomeCents)
    assertEquals(1200L, stats.dailySeries[0].expenseCents)
    assertEquals(5000L, stats.dailySeries[0].incomeCents)
  }

  @Test
  fun categoryPies_keepIncomeAndExpenseSeparate() {
    val stats =
      buildDashboardStats(
        entries =
          listOf(
            entry("1", LedgerEntryType.Expense, 1000, "微信", "餐饮", "2026-05-01"),
            entry("2", LedgerEntryType.Income, 8000, "银行卡", "工资", "2026-05-01"),
          ),
        filter = LedgerFilter("2026-05-01", "2026-05-01"),
      )

    assertEquals(listOf(AmountBreakdown("餐饮", 1000)), stats.expenseByCategory)
    assertEquals(listOf(AmountBreakdown("工资", 8000)), stats.incomeByCategory)
  }

  @Test
  fun channelBreakdown_keepsBothSeriesForSameChannel() {
    val stats =
      buildDashboardStats(
        entries =
          listOf(
            entry("1", LedgerEntryType.Expense, 1500, "微信", "餐饮", "2026-05-01"),
            entry("2", LedgerEntryType.Income, 6000, "微信", "红包", "2026-05-01"),
          ),
        filter = LedgerFilter("2026-05-01", "2026-05-01"),
      )

    assertEquals(listOf(ChannelAmountBreakdown("微信", 1500, 6000)), stats.channelBreakdown)
  }

  @Test
  fun incomeExpensePie_usesAbsoluteAmounts() {
    val stats =
      buildDashboardStats(
        entries =
          listOf(
            entry("1", LedgerEntryType.Expense, 3000, "微信", "餐饮", "2026-05-01"),
            entry("2", LedgerEntryType.Income, 1000, "现金", "红包", "2026-05-01"),
          ),
        filter = LedgerFilter("2026-05-01", "2026-05-01"),
      )

    assertEquals(listOf(AmountBreakdown("收入", 1000), AmountBreakdown("支出", 3000)), stats.incomeExpenseBreakdown)
  }

  @Test
  fun emptyData_isSafe() {
    val stats = buildDashboardStats(emptyList(), LedgerFilter("2026-05-01", "2026-05-02"))

    assertEquals(2, stats.dailySeries.size)
    assertFalse(stats.hasAnyEntries)
    assertEquals(emptyList<AmountBreakdown>(), stats.expenseByCategory)
    assertEquals(emptyList<AmountBreakdown>(), stats.incomeByCategory)
    assertEquals(emptyList<ChannelAmountBreakdown>(), stats.channelBreakdown)
    assertEquals(emptyList<AmountBreakdown>(), stats.incomeExpenseBreakdown)
  }

  private fun entry(
    id: String,
    type: LedgerEntryType,
    amountCents: Long,
    channel: String,
    category: String,
    date: String,
  ): LedgerEntry =
    LedgerEntry(
      id = id,
      type = type,
      amountCents = amountCents,
      channel = channel,
      category = category,
      description = "$category $amountCents",
      occurredAtMillis =
        ZonedDateTime.of(LocalDate.parse(date), LocalTime.NOON, ZoneId.systemDefault()).toInstant().toEpochMilli(),
      rawText = "",
      createdAtMillis = 0L,
    )
}
