package io.github.cming0420.agenticledger.ui.main

import io.github.cming0420.agenticledger.data.LedgerEntry
import io.github.cming0420.agenticledger.data.LedgerEntryType
import io.github.cming0420.agenticledger.data.LedgerFilter
import io.github.cming0420.agenticledger.data.asYuanText
import io.github.cming0420.agenticledger.data.localDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs
import kotlin.math.roundToLong

private const val TAG = "DashboardStats"

private fun safeLog(tag: String, msg: String) {
  try { android.util.Log.d(tag, msg) } catch (_: Throwable) { }
}

// ── 视图模式 ──────────────────────────────────────────────

enum class DashboardTimeRange(val label: String) {
  Day("日"),
  Week("周"),
  Month("月"),
}

// ── 核心统计模型（扩展版） ────────────────────────────────

data class DashboardStats(
  val dailySeries: List<DailyAmountPoint> = emptyList(),
  val expenseByCategory: List<AmountBreakdown> = emptyList(),
  val incomeByCategory: List<AmountBreakdown> = emptyList(),
  val channelBreakdown: List<ChannelAmountBreakdown> = emptyList(),
  val incomeExpenseBreakdown: List<AmountBreakdown> = emptyList(),
  val monthlySummary: MonthlySummary = MonthlySummary(),
  val monthlyBarSeries: List<MonthlyBarPoint> = emptyList(),
  val dailyCalendar: List<DailyCalendarDay> = emptyList(),
  val categoryComparison: List<CategoryComparisonItem> = emptyList(),
  val paymentMethodBreakdown: List<PaymentMethodItem> = emptyList(),
  val dailySummary: DailySummary = DailySummary(),
  val weeklySummary: WeeklySummary = WeeklySummary(),
  val yearlyYoY: TrendComparisonData = TrendComparisonData(),
  val sevenDayComparison: SevenDayComparison = SevenDayComparison(),
  val sevenDayBarSeries: List<SevenDayBarPoint> = emptyList(),
  val dailyRanking: List<ExpenseRankingItem> = emptyList(),
  val dayCategoryComparison: List<CategoryComparisonItem> = emptyList(),
  val weeklyBarSeries: List<WeeklyBarPoint> = emptyList(),
  val weeklyRanking: List<ExpenseRankingItem> = emptyList(),
  val weeklyCategoryDetails: List<WeeklyCategoryDetail> = emptyList(),
) {
  val hasAnyEntries: Boolean =
    dailySeries.any { it.expenseCents > 0L || it.incomeCents > 0L }
}

// ── 每日数据点 ────────────────────────────────────────────

data class DailyAmountPoint(
  val date: LocalDate,
  val expenseCents: Long,
  val incomeCents: Long,
)

// ── 金额分解 ──────────────────────────────────────────────

data class AmountBreakdown(
  val label: String,
  val amountCents: Long,
)

// ── 渠道分解 ──────────────────────────────────────────────

data class ChannelAmountBreakdown(
  val channel: String,
  val expenseCents: Long,
  val incomeCents: Long,
) {
  val totalCents: Long = expenseCents + incomeCents
}

// ── 变化量 ────────────────────────────────────────────────

data class ChangeAmount(
  val amountCents: Long = 0L,
  val percent: Double = 0.0,
)

// ── 月度摘要 ──────────────────────────────────────────────

data class MonthlySummary(
  val totalCents: Long = 0L,
  val entryCount: Int = 0,
  val vsLastMonth: ChangeAmount = ChangeAmount(),
  val changeDescription: String = "",
  val averageCents: Long = 0L,
)

// ── 月度柱状图数据点 ──────────────────────────────────────

data class MonthlyBarPoint(
  val yearMonth: YearMonth,
  val expenseCents: Long,
  val incomeCents: Long,
)

// ── 日历每日数据 ──────────────────────────────────────────

data class DailyCalendarDay(
  val date: LocalDate,
  val expenseCents: Long,
  val incomeCents: Long,
  val isToday: Boolean,
)

// ── 分类环比项 ────────────────────────────────────────────

data class CategoryComparisonItem(
  val category: String,
  val currentCents: Long,
  val previousCents: Long,
) {
  val changeCents: Long get() = currentCents - previousCents
  val changePercent: Double
    get() = if (previousCents > 0) (currentCents - previousCents).toDouble() / previousCents * 100
    else if (currentCents > 0) 100.0 else 0.0
}

// ── 支付方式项 ────────────────────────────────────────────

data class PaymentMethodItem(
  val method: String,
  val expenseCents: Long,
  val incomeCents: Long,
  val count: Int,
)

// ── 日度摘要（当日 vs 昨日） ──────────────────────────────

data class DailySummary(
  val todayExpenseCents: Long = 0L,
  val todayIncomeCents: Long = 0L,
  val yesterdayExpenseCents: Long = 0L,
  val yesterdayIncomeCents: Long = 0L,
) {
  val expenseChange: ChangeAmount
    get() {
      val diff = todayExpenseCents - yesterdayExpenseCents
      val pct = if (yesterdayExpenseCents > 0) diff.toDouble() / yesterdayExpenseCents * 100
      else if (todayExpenseCents > 0) 100.0 else 0.0
      return ChangeAmount(diff, pct)
    }
  val incomeChange: ChangeAmount
    get() {
      val diff = todayIncomeCents - yesterdayIncomeCents
      val pct = if (yesterdayIncomeCents > 0) diff.toDouble() / yesterdayIncomeCents * 100
      else if (todayIncomeCents > 0) 100.0 else 0.0
      return ChangeAmount(diff, pct)
    }
}

// ── 周度摘要（本周 vs 上周） ──────────────────────────────

data class WeeklySummary(
  val thisWeekExpenseCents: Long = 0L,
  val thisWeekIncomeCents: Long = 0L,
  val lastWeekExpenseCents: Long = 0L,
  val lastWeekIncomeCents: Long = 0L,
) {
  val expenseChange: ChangeAmount
    get() {
      val diff = thisWeekExpenseCents - lastWeekExpenseCents
      val pct = if (lastWeekExpenseCents > 0) diff.toDouble() / lastWeekExpenseCents * 100
      else if (thisWeekExpenseCents > 0) 100.0 else 0.0
      return ChangeAmount(diff, pct)
    }
  val incomeChange: ChangeAmount
    get() {
      val diff = thisWeekIncomeCents - lastWeekIncomeCents
      val pct = if (lastWeekIncomeCents > 0) diff.toDouble() / lastWeekIncomeCents * 100
      else if (thisWeekIncomeCents > 0) 100.0 else 0.0
      return ChangeAmount(diff, pct)
    }
}

// ── 趋势对比数据（月度/年度同比环比） ─────────────────────

data class TrendComparisonData(
  val currentSeries: List<TrendPoint> = emptyList(),
  val previousSeries: List<TrendPoint> = emptyList(),
) {
  val currentTotalCents: Long get() = currentSeries.sumOf { it.amountCents }
  val previousTotalCents: Long get() = previousSeries.sumOf { it.amountCents }
  val totalChange: ChangeAmount
    get() {
      val diff = currentTotalCents - previousTotalCents
      val pct = if (previousTotalCents > 0) diff.toDouble() / previousTotalCents * 100
      else if (currentTotalCents > 0) 100.0 else 0.0
      return ChangeAmount(diff, pct)
    }
}

data class TrendPoint(
  val label: String,
  val amountCents: Long,
)

// ── 7天环比摘要 ───────────────────────────────────────────

data class SevenDayComparison(
  val todayExpenseCents: Long = 0L,
  val sevenDaysAgoExpenseCents: Long = 0L,
) {
  val changeCents: Long get() = todayExpenseCents - sevenDaysAgoExpenseCents
  val changePercent: Double
    get() = if (sevenDaysAgoExpenseCents > 0) changeCents.toDouble() / sevenDaysAgoExpenseCents * 100
    else if (todayExpenseCents > 0) 100.0 else 0.0
}

// ── 7天环比柱状图数据点 ───────────────────────────────────

data class SevenDayBarPoint(
  val label: String,
  val expenseCents: Long,
)

// ── 支出排行项 ────────────────────────────────────────────

data class ExpenseRankingItem(
  val id: String,
  val description: String,
  val category: String,
  val channel: String,
  val amountCents: Long,
  val occurredAtMillis: Long,
  val rank: Int,
)

// ── 周度柱状图数据点 ─────────────────────────────────────

data class WeeklyBarPoint(
    val dayOfWeek: String,
    val dateLabel: String,
    val isToday: Boolean,
    val thisWeekCents: Long,
    val lastWeekCents: Long,
)

// ── 周度分类详情（含子分类） ────────────────────────────

data class WeeklyCategoryDetail(
    val category: String,
    val currentCents: Long,
    val previousCents: Long,
    val count: Int,
    val subcategories: List<SubcategoryItem> = emptyList(),
) {
    val changeCents: Long get() = currentCents - previousCents
    val changePercent: Double
        get() = if (previousCents > 0) (currentCents - previousCents).toDouble() / previousCents * 100
        else if (currentCents > 0) 100.0 else 0.0
}

data class SubcategoryItem(
    val name: String,
    val amountCents: Long,
    val changeCents: Long,
)

// ══════════════════════════════════════════════════════════
//  主入口：构建统计
// ══════════════════════════════════════════════════════════

fun buildDashboardStats(
  allEntries: List<LedgerEntry>,
  filter: LedgerFilter,
  timeRange: DashboardTimeRange = DashboardTimeRange.Month,
  currentDate: LocalDate = LocalDate.now(),
  isExpenseMode: Boolean = true,
): DashboardStats {
  val start = runCatching { LocalDate.parse(filter.startDate) }.getOrNull()
  val end = runCatching { LocalDate.parse(filter.endDate) }.getOrNull()
  val filteredEntries = allEntries.filterByRange(start, end)

  val dailySeries =
    if (start != null && end != null && !start.isAfter(end)) {
      buildDailySeries(filteredEntries, start, end)
    } else {
      buildDailySeries(filteredEntries)
    }

  val type = if (isExpenseMode) LedgerEntryType.Expense else LedgerEntryType.Income

  return DashboardStats(
    dailySeries = dailySeries,
    expenseByCategory = filteredEntries.breakdownByCategory(LedgerEntryType.Expense),
    incomeByCategory = filteredEntries.breakdownByCategory(LedgerEntryType.Income),
    channelBreakdown = filteredEntries.breakdownByChannel(),
    incomeExpenseBreakdown = listOf(
      AmountBreakdown("收入", filteredEntries.totalByType(LedgerEntryType.Income)),
      AmountBreakdown("支出", filteredEntries.totalByType(LedgerEntryType.Expense)),
    ).filter { it.amountCents > 0L },
    monthlySummary = allEntries.buildMonthlySummary(currentDate, type),
    monthlyBarSeries = allEntries.buildMonthlyBarSeries(currentDate),
    dailyCalendar = allEntries.buildDailyCalendar(currentDate),
    categoryComparison = allEntries.buildCategoryComparison(currentDate, type),
    paymentMethodBreakdown = filteredEntries.buildPaymentMethodBreakdown(),
    dailySummary = allEntries.buildDailySummary(currentDate),
    weeklySummary = allEntries.buildWeeklySummary(currentDate),
    yearlyYoY = allEntries.buildYearlyYoY(currentDate, type),
    sevenDayComparison = allEntries.buildSevenDayComparison(currentDate, type),
    sevenDayBarSeries = allEntries.buildSevenDayBarSeries(currentDate, type),
    dailyRanking = allEntries.buildExpenseRanking(currentDate, type),
    dayCategoryComparison = allEntries.buildDayCategoryComparison(currentDate, type),
    weeklyBarSeries = allEntries.buildWeeklyBarSeries(currentDate, type),
    weeklyRanking = allEntries.buildWeeklyRanking(currentDate, type),
    weeklyCategoryDetails = allEntries.buildWeeklyCategoryDetails(currentDate, type),
  )
}

// ══════════════════════════════════════════════════════════
//  聚合函数
// ══════════════════════════════════════════════════════════

// ── 月度摘要 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildMonthlySummary(
  currentDate: LocalDate,
  type: LedgerEntryType,
): MonthlySummary {
  val ym = YearMonth.from(currentDate)
  val currentEntries = filterByMonth(ym)
  val totalCents = currentEntries.totalByType(type)
  val prevEntries = filterByMonth(ym.minusMonths(1))
  val prevTotal = prevEntries.totalByType(type)
  val diff = totalCents - prevTotal
  val pct = if (prevTotal > 0) diff.toDouble() / prevTotal * 100 else if (totalCents > 0) 100.0 else 0.0

  val totalPrev12Cents = (1L..12L).sumOf { offset ->
    filterByMonth(ym.minusMonths(offset)).totalByType(type)
  }
  val avgCents = if (totalPrev12Cents > 0) totalPrev12Cents / 12 else 0L

  safeLog(TAG, "buildMonthlySummary: month=$ym, total=$totalCents, prev=$prevTotal, diff=$diff, avg12=$avgCents")

  val desc = buildString {
    val absPct = abs(pct)
    if (absPct < 5.0) {
      append("本月${type.label}金额变化不大")
    } else {
      append("本月${type.label}金额${if (diff > 0) "有所上升" else "有所下降"}")
    }
    if (diff != 0L) {
      val word = if (diff > 0) "增加" else "减少"
      append("，比上月${word} ${abs(diff).asYuanText()} 元")
    }
  }

  return MonthlySummary(totalCents, currentEntries.size, ChangeAmount(diff, pct), desc, avgCents)
}

// ── 月度柱状图（最近12个月） ──────────────────────────────

private fun List<LedgerEntry>.buildMonthlyBarSeries(currentDate: LocalDate): List<MonthlyBarPoint> {
  val currentYm = YearMonth.from(currentDate)
  val series = (11 downTo 0).map { offset ->
    val ym = currentYm.minusMonths(offset.toLong())
    val monthEntries = filterByMonth(ym)
    MonthlyBarPoint(ym, monthEntries.totalByType(LedgerEntryType.Expense), monthEntries.totalByType(LedgerEntryType.Income))
  }
  safeLog(TAG, "buildMonthlyBarSeries: currentMonth=$currentYm, points=${series.size}")
  series.forEach { p ->
    safeLog(TAG, "  ${p.yearMonth}: expense=${p.expenseCents} income=${p.incomeCents}")
  }
  return series
}

// ── 日历网格 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildDailyCalendar(currentDate: LocalDate): List<DailyCalendarDay> {
  val ym = YearMonth.from(currentDate)
  val start = ym.atDay(1)
  val end = ym.atEndOfMonth()
  val today = LocalDate.now()
  val byDate = groupBy { it.localDate() }
  return (1..end.dayOfMonth).map { day ->
    val date = ym.atDay(day)
    val dayEntries = byDate[date].orEmpty()
    DailyCalendarDay(date, dayEntries.totalByType(LedgerEntryType.Expense), dayEntries.totalByType(LedgerEntryType.Income), date == today)
  }
}

// ── 分类环比 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildCategoryComparison(
  currentDate: LocalDate,
  type: LedgerEntryType,
): List<CategoryComparisonItem> {
  val ym = YearMonth.from(currentDate)
  val currentEntries = filterByMonth(ym).filter { it.type == type }
  val previousEntries = filterByMonth(ym.minusMonths(1)).filter { it.type == type }
  val currentByCategory = currentEntries.groupBy { it.category.ifBlank { "其他" } }
  val previousByCategory = previousEntries.groupBy { it.category.ifBlank { "其他" } }
  val allCategories = currentByCategory.keys + previousByCategory.keys
  return allCategories.map { category ->
    CategoryComparisonItem(category, currentByCategory[category].orEmpty().sumOf { it.amountCents }, previousByCategory[category].orEmpty().sumOf { it.amountCents })
  }.filter { it.currentCents > 0 || it.previousCents > 0 }.sortedByDescending { it.currentCents }
}

// ── 支付方式分解 ──────────────────────────────────────────

private fun List<LedgerEntry>.buildPaymentMethodBreakdown(): List<PaymentMethodItem> {
  return groupBy { it.channel.ifBlank { "其他" } }
    .map { (method, entries) ->
      PaymentMethodItem(method, entries.totalByType(LedgerEntryType.Expense), entries.totalByType(LedgerEntryType.Income), entries.size)
    }
    .filter { it.expenseCents > 0 || it.incomeCents > 0 }
    .sortedByDescending { it.expenseCents + it.incomeCents }
}

// ── 日度摘要 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildDailySummary(currentDate: LocalDate): DailySummary {
  val todayEntries = filter { it.localDate() == currentDate }
  val yesterdayEntries = filter { it.localDate() == currentDate.minusDays(1) }
  return DailySummary(todayEntries.totalByType(LedgerEntryType.Expense), todayEntries.totalByType(LedgerEntryType.Income), yesterdayEntries.totalByType(LedgerEntryType.Expense), yesterdayEntries.totalByType(LedgerEntryType.Income))
}

// ── 周度摘要 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildWeeklySummary(currentDate: LocalDate): WeeklySummary {
  val thisWeekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
  val thisWeekEnd = thisWeekStart.plusDays(6)
  val lastWeekStart = thisWeekStart.minusWeeks(1)
  val lastWeekEnd = lastWeekStart.plusDays(6)
  val thisWeekEntries = filterByRange(thisWeekStart, thisWeekEnd)
  val lastWeekEntries = filterByRange(lastWeekStart, lastWeekEnd)
  return WeeklySummary(thisWeekEntries.totalByType(LedgerEntryType.Expense), thisWeekEntries.totalByType(LedgerEntryType.Income), lastWeekEntries.totalByType(LedgerEntryType.Expense), lastWeekEntries.totalByType(LedgerEntryType.Income))
}

// ── 年度同比 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildYearlyYoY(
  currentDate: LocalDate,
  type: LedgerEntryType,
): TrendComparisonData {
  val currentYm = YearMonth.from(currentDate)
  val currentSeries = (11 downTo 0).map { offset ->
    val ym = currentYm.minusMonths(offset.toLong())
    TrendPoint("${ym.monthValue}月", filterByMonth(ym).totalByType(type))
  }
  val prevYearYm = currentYm.minusYears(1)
  val previousSeries = (11 downTo 0).map { offset ->
    val ym = prevYearYm.minusMonths(offset.toLong())
    TrendPoint("${ym.monthValue}月", filterByMonth(ym).totalByType(type))
  }
  return TrendComparisonData(currentSeries, previousSeries)
}

// ── 7天环比 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildSevenDayComparison(
  currentDate: LocalDate,
  type: LedgerEntryType,
): SevenDayComparison {
  val todayEntries = filter { it.localDate() == currentDate && it.type == type }
  val sevenDaysAgoEntries = filter { it.localDate() == currentDate.minusDays(7) && it.type == type }
  return SevenDayComparison(
    todayExpenseCents = todayEntries.sumOf { it.amountCents },
    sevenDaysAgoExpenseCents = sevenDaysAgoEntries.sumOf { it.amountCents },
  )
}

// ── 7天柱状图 ─────────────────────────────────────────────

private fun List<LedgerEntry>.buildSevenDayBarSeries(
  currentDate: LocalDate,
  type: LedgerEntryType,
): List<SevenDayBarPoint> {
  return listOf(
    SevenDayBarPoint("7天前", filter { it.localDate() == currentDate.minusDays(7) && it.type == type }.sumOf { it.amountCents }),
    SevenDayBarPoint("今日", filter { it.localDate() == currentDate && it.type == type }.sumOf { it.amountCents }),
  )
}

// ── 支出排行 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildExpenseRanking(
  currentDate: LocalDate,
  type: LedgerEntryType,
): List<ExpenseRankingItem> {
  return filter { it.localDate() == currentDate && it.type == type }
    .sortedByDescending { it.amountCents }
    .mapIndexed { index, entry ->
      ExpenseRankingItem(
        id = entry.id,
        description = entry.description,
        category = entry.category.ifBlank { "其他" },
        channel = entry.channel.ifBlank { "其他" },
        amountCents = entry.amountCents,
        occurredAtMillis = entry.occurredAtMillis,
        rank = index + 1,
      )
    }
}

// ── 日度分类环比（当日 vs 7天前同日分类） ─────────────────

private fun List<LedgerEntry>.buildDayCategoryComparison(
  currentDate: LocalDate,
  type: LedgerEntryType,
): List<CategoryComparisonItem> {
  val todayEntries = filter { it.localDate() == currentDate && it.type == type }
  val sevenDaysAgoEntries = filter { it.localDate() == currentDate.minusDays(7) && it.type == type }
  val currentByCategory = todayEntries.groupBy { it.category.ifBlank { "其他" } }
  val previousByCategory = sevenDaysAgoEntries.groupBy { it.category.ifBlank { "其他" } }
  val allCategories = currentByCategory.keys + previousByCategory.keys
  return allCategories.map { category ->
    CategoryComparisonItem(
      category,
      currentByCategory[category].orEmpty().sumOf { it.amountCents },
      previousByCategory[category].orEmpty().sumOf { it.amountCents },
    )
  }.filter { it.currentCents > 0 || it.previousCents > 0 }.sortedByDescending { it.currentCents }
}

// ── 周度柱状图 ────────────────────────────────────────────

private fun List<LedgerEntry>.buildWeeklyBarSeries(
    currentDate: LocalDate,
    type: LedgerEntryType,
): List<WeeklyBarPoint> {
    val weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val today = LocalDate.now()
    val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val formatter = DateTimeFormatter.ofPattern("MM.dd")
    return (0..6).map { offset ->
        val date = weekStart.plusDays(offset.toLong())
        val lastWeekDate = date.minusWeeks(1)
        val thisWeekEntries = filter { it.localDate() == date && it.type == type }
        val lastWeekEntries = filter { it.localDate() == lastWeekDate && it.type == type }
        WeeklyBarPoint(
            dayOfWeek = dayNames[offset],
            dateLabel = date.format(formatter),
            isToday = date == today,
            thisWeekCents = thisWeekEntries.sumOf { it.amountCents },
            lastWeekCents = lastWeekEntries.sumOf { it.amountCents },
        )
    }
}

// ── 周度排行 ──────────────────────────────────────────────

private fun List<LedgerEntry>.buildWeeklyRanking(
    currentDate: LocalDate,
    type: LedgerEntryType,
): List<ExpenseRankingItem> {
    val weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    return filter { entry ->
        val date = entry.localDate()
        !date.isBefore(weekStart) && !date.isAfter(weekEnd) && entry.type == type
    }
        .sortedByDescending { it.amountCents }
        .mapIndexed { index, entry ->
            ExpenseRankingItem(
                id = entry.id,
                description = entry.description,
                category = entry.category.ifBlank { "其他" },
                channel = entry.channel.ifBlank { "其他" },
                amountCents = entry.amountCents,
                occurredAtMillis = entry.occurredAtMillis,
                rank = index + 1,
            )
        }
}

// ── 周度分类详情 ──────────────────────────────────────────

private fun List<LedgerEntry>.buildWeeklyCategoryDetails(
    currentDate: LocalDate,
    type: LedgerEntryType,
): List<WeeklyCategoryDetail> {
    val weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    val lastWeekStart = weekStart.minusWeeks(1)
    val lastWeekEnd = lastWeekStart.plusDays(6)

    val thisWeekEntries = filterByRange(weekStart, weekEnd).filter { it.type == type }
    val lastWeekEntries = filterByRange(lastWeekStart, lastWeekEnd).filter { it.type == type }

    val groupedByCategory = thisWeekEntries.groupBy { it.category.ifBlank { "其他" } }
    val lastWeekByCategory = lastWeekEntries.groupBy { it.category.ifBlank { "其他" } }

    return groupedByCategory.map { (category, entries) ->
        val currentCents = entries.sumOf { it.amountCents }
        val previousCents = lastWeekByCategory[category].orEmpty().sumOf { it.amountCents }
        val subcategories = entries.groupBy { it.description }
            .map { (desc, descEntries) ->
                val descCents = descEntries.sumOf { it.amountCents }
                val prevDescCents = lastWeekByCategory[category].orEmpty()
                    .filter { it.description == desc }
                    .sumOf { it.amountCents }
                SubcategoryItem(name = desc, amountCents = descCents, changeCents = descCents - prevDescCents)
            }
            .sortedByDescending { it.amountCents }
        WeeklyCategoryDetail(category, currentCents, previousCents, entries.size, subcategories)
    }.sortedByDescending { it.currentCents }
}

// ══════════════════════════════════════════════════════════
//  工具函数
// ══════════════════════════════════════════════════════════

private fun List<LedgerEntry>.filterByRange(start: LocalDate?, end: LocalDate?): List<LedgerEntry> {
  return filter { entry ->
    val date = entry.localDate()
    val afterStart = start == null || !date.isBefore(start)
    val beforeEnd = end == null || !date.isAfter(end)
    afterStart && beforeEnd
  }
}

private fun List<LedgerEntry>.filterByMonth(ym: YearMonth): List<LedgerEntry> =
  filter { YearMonth.from(it.localDate()) == ym }

private fun buildDailySeries(entries: List<LedgerEntry>, start: LocalDate, end: LocalDate): List<DailyAmountPoint> {
  val byDate = entries.groupBy { it.localDate() }
  val days = generateSequence(start) { date -> date.plusDays(1).takeIf { !it.isAfter(end) } }
  return days.map { date ->
    val dayEntries = byDate[date].orEmpty()
    DailyAmountPoint(date, dayEntries.totalByType(LedgerEntryType.Expense), dayEntries.totalByType(LedgerEntryType.Income))
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
        ChannelAmountBreakdown(channel, entries.totalByType(LedgerEntryType.Expense), entries.totalByType(LedgerEntryType.Income))
      }
      .filter { it.totalCents > 0L }
      .sortedByDescending { it.totalCents }
  if (grouped.size <= MAX_CHANNEL_BUCKETS) return grouped
  val top = grouped.take(MAX_CHANNEL_BUCKETS - 1)
  val rest = grouped.drop(MAX_CHANNEL_BUCKETS - 1)
  return top + ChannelAmountBreakdown("其他渠道", rest.sumOf { it.expenseCents }, rest.sumOf { it.incomeCents })
}

private fun List<LedgerEntry>.totalByType(type: LedgerEntryType): Long =
  filter { it.type == type }.sumOf { it.amountCents }

private const val MAX_CHANNEL_BUCKETS = 8
