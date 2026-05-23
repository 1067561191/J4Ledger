package io.github.cming0420.agenticledger.ui.main

import io.github.cming0420.agenticledger.data.LedgerEntry
import io.github.cming0420.agenticledger.data.LedgerEntryType
import io.github.cming0420.agenticledger.data.LedgerFilter
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
        allEntries =
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
        allEntries =
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
        allEntries =
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
        allEntries =
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
    val stats = buildDashboardStats(allEntries = emptyList(), filter = LedgerFilter("2026-05-01", "2026-05-02"))

    assertEquals(2, stats.dailySeries.size)
    assertFalse(stats.hasAnyEntries)
    assertEquals(emptyList<AmountBreakdown>(), stats.expenseByCategory)
    assertEquals(emptyList<AmountBreakdown>(), stats.incomeByCategory)
    assertEquals(emptyList<ChannelAmountBreakdown>(), stats.channelBreakdown)
    assertEquals(emptyList<AmountBreakdown>(), stats.incomeExpenseBreakdown)
  }

  @Test
  fun sevenDayComparison_comparesTodayWithSevenDaysAgo() {
    val today = LocalDate.of(2026, 5, 15)
    val sevenDaysAgo = today.minusDays(7)
    val stats = buildDashboardStats(
      allEntries = listOf(
        entry("1", LedgerEntryType.Expense, 5000, "微信", "餐饮", today.toString()),
        entry("2", LedgerEntryType.Expense, 3000, "微信", "交通", today.toString()),
        entry("3", LedgerEntryType.Expense, 2000, "现金", "餐饮", sevenDaysAgo.toString()),
        entry("4", LedgerEntryType.Income, 10000, "银行卡", "工资", today.toString()),
      ),
      filter = LedgerFilter(today.toString(), today.toString()),
      timeRange = DashboardTimeRange.Day,
      currentDate = today,
      isExpenseMode = true,
    )

    assertEquals(8000L, stats.sevenDayComparison.todayExpenseCents)
    assertEquals(2000L, stats.sevenDayComparison.sevenDaysAgoExpenseCents)
    assertEquals(6000L, stats.sevenDayComparison.changeCents)
    assertEquals(2, stats.sevenDayBarSeries.size)
    assertEquals("7天前", stats.sevenDayBarSeries[0].label)
    assertEquals("今日", stats.sevenDayBarSeries[1].label)
    assertEquals(2000L, stats.sevenDayBarSeries[0].expenseCents)
    assertEquals(8000L, stats.sevenDayBarSeries[1].expenseCents)
  }

  @Test
  fun expenseRanking_sortedByAmountDescending() {
    val today = LocalDate.of(2026, 5, 15)
    val stats = buildDashboardStats(
      allEntries = listOf(
        entry("1", LedgerEntryType.Expense, 1000, "微信", "餐饮", today.toString()),
        entry("2", LedgerEntryType.Expense, 5000, "银行卡", "购物", today.toString()),
        entry("3", LedgerEntryType.Expense, 3000, "现金", "交通", today.toString()),
        entry("4", LedgerEntryType.Income, 8000, "微信", "工资", today.toString()),
        entry("5", LedgerEntryType.Expense, 1500, "微信", "餐饮", "2026-05-14"),
      ),
      filter = LedgerFilter(today.toString(), today.toString()),
      timeRange = DashboardTimeRange.Day,
      currentDate = today,
      isExpenseMode = true,
    )

    assertEquals(3, stats.dailyRanking.size)
    assertEquals(1, stats.dailyRanking[0].rank)
    assertEquals(5000L, stats.dailyRanking[0].amountCents)
    assertEquals("购物", stats.dailyRanking[0].category)
    assertEquals(2, stats.dailyRanking[1].rank)
    assertEquals(3000L, stats.dailyRanking[1].amountCents)
    assertEquals(3, stats.dailyRanking[2].rank)
    assertEquals(1000L, stats.dailyRanking[2].amountCents)
  }

  @Test
  fun dayCategoryComparison_comparesWithSevenDaysAgo() {
    val today = LocalDate.of(2026, 5, 15)
    val sevenDaysAgo = today.minusDays(7)
    val stats = buildDashboardStats(
      allEntries = listOf(
        entry("1", LedgerEntryType.Expense, 3000, "微信", "餐饮", today.toString()),
        entry("2", LedgerEntryType.Expense, 1000, "微信", "交通", today.toString()),
        entry("3", LedgerEntryType.Expense, 2000, "现金", "餐饮", sevenDaysAgo.toString()),
      ),
      filter = LedgerFilter(today.toString(), today.toString()),
      timeRange = DashboardTimeRange.Day,
      currentDate = today,
      isExpenseMode = true,
    )

    val dining = stats.dayCategoryComparison.find { it.category == "餐饮" }
    assertEquals(3000L, dining!!.currentCents)
    assertEquals(2000L, dining.previousCents)
    assertEquals(1000L, dining.changeCents)

    val transport = stats.dayCategoryComparison.find { it.category == "交通" }
    assertEquals(1000L, transport!!.currentCents)
    assertEquals(0L, transport.previousCents)
  }

  @Test
  fun expenseRanking_emptyWhenNoExpenses() {
    val today = LocalDate.of(2026, 5, 15)
    val stats = buildDashboardStats(
      allEntries = listOf(
        entry("1", LedgerEntryType.Income, 5000, "微信", "工资", today.toString()),
      ),
      filter = LedgerFilter(today.toString(), today.toString()),
      timeRange = DashboardTimeRange.Day,
      currentDate = today,
      isExpenseMode = true,
    )

    assertEquals(emptyList<ExpenseRankingItem>(), stats.dailyRanking)
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
