package com.example.androidproject.ui.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidproject.data.AppThemeMode
import com.example.androidproject.data.BackupOptions
import com.example.androidproject.data.LedgerFilter
import com.example.androidproject.data.LedgerEntry
import com.example.androidproject.data.LedgerEntryType
import com.example.androidproject.data.OpenAiCompatibleExpenseAgent
import com.example.androidproject.data.SQLiteLedgerRepository
import com.example.androidproject.data.asYuanText
import com.example.androidproject.data.localDateTimeText
import com.example.androidproject.data.toCents
import com.example.androidproject.theme.J4LedgerTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun MainScreen(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current.applicationContext
  val viewModel: MainScreenViewModel =
    viewModel { MainScreenViewModel(SQLiteLedgerRepository(context), OpenAiCompatibleExpenseAgent()) }
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  J4LedgerTheme(themeMode = state.settings.themeMode) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      MainScreenContent(state = state, viewModel = viewModel)
    }
  }
}

@Composable
private fun MainScreenContent(
  state: MainScreenUiState,
  viewModel: MainScreenViewModel,
  modifier: Modifier = Modifier,
) {
  var selectedTab by rememberSaveable { mutableStateOf(MainTab.Record) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    bottomBar = {
      NavigationBar {
        MainTab.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            icon = { Text(tab.icon, fontWeight = FontWeight.SemiBold) },
            label = { Text(tab.title) },
            alwaysShowLabel = true,
          )
        }
      }
    },
  ) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Header(title = state.appTitle, totalText = state.totalText)
      ProvideVicoTheme(theme = rememberM3VicoTheme()) {
        AnimatedContent(
          targetState = selectedTab,
          label = "main-tab-transition",
          transitionSpec = {
            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
            (slideInHorizontally(animationSpec = tween(260)) { width -> direction * width / 4 } + fadeIn(tween(220)))
              .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { width -> -direction * width / 4 } + fadeOut(tween(180)))
              .using(SizeTransform(clip = false))
          },
          modifier = Modifier.weight(1f),
        ) { tab ->
          Box(modifier = Modifier.fillMaxSize()) {
            when (tab) {
              MainTab.Record -> RecordPane(state = state, viewModel = viewModel, modifier = Modifier.fillMaxSize())
              MainTab.Bills -> BillsPane(state = state, viewModel = viewModel, modifier = Modifier.fillMaxSize())
              MainTab.Dashboard -> DashboardPane(state = state, viewModel = viewModel, modifier = Modifier.fillMaxSize())
              MainTab.Settings -> SettingsPane(state = state, viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }
          }
        }
      }
    }
  }
}

@Composable
private fun Header(title: String, totalText: String) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(title.ifBlank { "J4Ledger" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
    Text("当前筛选净额 ¥$totalText", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
  }
}

@Composable
private fun RecordPane(
  state: MainScreenUiState,
  viewModel: MainScreenViewModel,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
    OutlinedTextField(
      value = state.transcript,
      onValueChange = viewModel::updateTranscript,
      label = { Text("消费内容") },
      placeholder = { Text("例如：今天用微信支付 28 元买咖啡 （最好包含 用途、支付渠道、支付金额、日期）") },
      modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
      minLines = 4,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
      Button(onClick = viewModel::submitTranscript, enabled = !state.isProcessing) { Text("记消费") }
      if (state.isProcessing) CircularProgressIndicator(modifier = Modifier.height(28.dp).width(28.dp), strokeWidth = 3.dp)
    }
    OutlinedTextField(
      value = state.incomeTranscript,
      onValueChange = viewModel::updateIncomeTranscript,
      label = { Text("收入内容") },
      placeholder = { Text("例如：今天银行卡到账 8000 元工资 （最好包含 收入类型、收款渠道、金额、日期时间）") },
      modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
      minLines = 4,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
      Button(onClick = viewModel::submitIncomeTranscript, enabled = !state.isProcessing) { Text("记收入") }
      if (state.isProcessing) CircularProgressIndicator(modifier = Modifier.height(28.dp).width(28.dp), strokeWidth = 3.dp)
    }
    Text(state.statusMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun BillsPane(
  state: MainScreenUiState,
  viewModel: MainScreenViewModel,
  modifier: Modifier = Modifier,
) {
  var editingEntry by remember { mutableStateOf<LedgerEntry?>(null) }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
    DateRangeSelector(filter = state.filter, onRangeChange = viewModel::updateFilter, title = "账单日期")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
      if (state.entries.isEmpty()) {
        item { Text("这个日期范围内还没有账单", color = MaterialTheme.colorScheme.onSurfaceVariant) }
      }
      items(state.entries, key = { it.id }) { entry ->
        LedgerEntryCard(entry = entry, onLongPress = { editingEntry = it })
      }
    }
  }

  editingEntry?.let { entry ->
    EditEntryDialog(
      entry = entry,
      onDismiss = { editingEntry = null },
      onSave = {
        viewModel.updateEntry(it)
        editingEntry = null
      },
      onDelete = {
        viewModel.deleteEntry(entry.id)
        editingEntry = null
      },
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LedgerEntryCard(entry: LedgerEntry, onLongPress: (LedgerEntry) -> Unit) {
  val isIncome = entry.type == LedgerEntryType.Income
  val amountPrefix = if (isIncome) "+¥" else "-¥"
  val amountColor = if (isIncome) DeepIncomeYellow else MaterialTheme.colorScheme.error
  ElevatedCard(
    modifier =
      Modifier
        .fillMaxWidth()
        .combinedClickable(onClick = {}, onLongClick = { onLongPress(entry) })
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(entry.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Text("$amountPrefix${entry.amountCents.asYuanText()}", style = MaterialTheme.typography.titleMedium, color = amountColor)
      }
      Text("${entry.localDateTimeText()} · ${entry.type.label} · ${entry.channel} · ${entry.category}", color = MaterialTheme.colorScheme.onSurfaceVariant)
      if (entry.rawText.isNotBlank()) Text(entry.rawText, style = MaterialTheme.typography.bodySmall)
    }
  }
}

@Composable
private fun EditEntryDialog(
  entry: LedgerEntry,
  onDismiss: () -> Unit,
  onSave: (LedgerEntry) -> Unit,
  onDelete: () -> Unit,
) {
  var amountText by rememberSaveable(entry.id) { mutableStateOf(entry.amountCents.asYuanText()) }
  var channel by rememberSaveable(entry.id) { mutableStateOf(entry.channel) }
  var category by rememberSaveable(entry.id) { mutableStateOf(entry.category) }
  var description by rememberSaveable(entry.id) { mutableStateOf(entry.description) }
  var dateText by rememberSaveable(entry.id) { mutableStateOf(entry.localDateTimeText()) }
  var errorText by rememberSaveable(entry.id) { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("编辑账单") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("金额") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("事项") }, singleLine = true)
        OutlinedTextField(value = channel, onValueChange = { channel = it }, label = { Text("渠道") }, singleLine = true)
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("分类") }, singleLine = true)
        OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("日期时间 yyyy-MM-dd HH:mm") }, singleLine = true)
        Text("类型：${entry.type.label}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (errorText.isNotBlank()) Text(errorText, color = MaterialTheme.colorScheme.error)
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amount = amountText.toDoubleOrNull()
          val occurredAt = dateText.toOccurredAtMillisOrNull()
          when {
            amount == null || amount <= 0.0 -> errorText = "金额必须大于 0"
            occurredAt == null -> errorText = "日期时间格式应为 yyyy-MM-dd HH:mm"
            description.isBlank() -> errorText = "事项不能为空"
            else ->
              onSave(
                entry.copy(
                  amountCents = amount.toCents(),
                  channel = channel.ifBlank { "其他" },
                  category = category.ifBlank { "其他" },
                  description = description.trim(),
                  occurredAtMillis = occurredAt,
                )
              )
          }
        }
      ) {
        Text("保存")
      }
    },
    dismissButton = {
      Row {
        TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
        TextButton(onClick = onDismiss) { Text("取消") }
      }
    },
  )
}

@Composable
private fun DashboardPane(
  state: MainScreenUiState,
  viewModel: MainScreenViewModel,
  modifier: Modifier = Modifier,
) {
  val stats = state.dashboardStats
  val expenseColor = MaterialTheme.colorScheme.error
  val incomeColor = DeepIncomeYellow

  LazyColumn(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item {
      DateRangeSelector(filter = state.filter, onRangeChange = viewModel::updateFilter, title = "Dashboard 日期")
    }
    if (!stats.hasAnyEntries) {
      item { EmptyDashboardCard() }
    } else {
      item {
        DashboardChartCard(title = "每日收支趋势", hasData = stats.dailySeries.isNotEmpty()) {
          DailyTrendChart(points = stats.dailySeries, expenseColor = expenseColor, incomeColor = incomeColor)
          ChartLegend(
            items =
              listOf(
                LegendItem("消费", expenseColor, stats.dailySeries.sumOf { it.expenseCents }),
                LegendItem("收入", incomeColor, stats.dailySeries.sumOf { it.incomeCents }),
              )
          )
        }
      }
      item {
        val colors = dashboardPalette(expenseColor, incomeColor)
        DashboardChartCard(title = "消费分类占比", hasData = stats.expenseByCategory.isNotEmpty()) {
          BreakdownPieChart(items = stats.expenseByCategory, colors = colors)
          BreakdownLegend(items = stats.expenseByCategory, colors = colors)
        }
      }
      item {
        val colors = dashboardPalette(incomeColor, expenseColor)
        DashboardChartCard(title = "收入分类占比", hasData = stats.incomeByCategory.isNotEmpty()) {
          BreakdownPieChart(items = stats.incomeByCategory, colors = colors)
          BreakdownLegend(items = stats.incomeByCategory, colors = colors)
        }
      }
      item {
        DashboardChartCard(title = "渠道收支对比", hasData = stats.channelBreakdown.isNotEmpty()) {
          ChannelComparisonChart(items = stats.channelBreakdown, expenseColor = expenseColor, incomeColor = incomeColor)
          ChartLegend(
            items =
              listOf(
                LegendItem("消费", expenseColor, stats.channelBreakdown.sumOf { it.expenseCents }),
                LegendItem("收入", incomeColor, stats.channelBreakdown.sumOf { it.incomeCents }),
              )
          )
          ChannelLegend(items = stats.channelBreakdown)
        }
      }
      item {
        val colors = listOf(incomeColor, expenseColor)
        DashboardChartCard(title = "收入支出占比", hasData = stats.incomeExpenseBreakdown.isNotEmpty()) {
          BreakdownPieChart(items = stats.incomeExpenseBreakdown, colors = colors)
          BreakdownLegend(items = stats.incomeExpenseBreakdown, colors = colors)
        }
      }
    }
  }
}

@Composable
private fun EmptyDashboardCard() {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text("暂无统计数据", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
      Text("当前日期范围内还没有账单", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun DashboardChartCard(
  title: String,
  hasData: Boolean,
  content: @Composable ColumnScope.() -> Unit,
) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
      if (hasData) {
        content()
      } else {
        Text("暂无可统计数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
private fun DailyTrendChart(
  points: List<DailyAmountPoint>,
  expenseColor: Color,
  incomeColor: Color,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  val xValues = remember(points) { points.indices.map { it.toDouble() } }
  val expenseValues = remember(points) { points.map { it.expenseCents.toYuanDouble() } }
  val incomeValues = remember(points) { points.map { it.incomeCents.toYuanDouble() } }
  LaunchedEffect(modelProducer, xValues, expenseValues, incomeValues) {
    modelProducer.runTransaction {
      lineSeries {
        series(xValues, expenseValues)
        series(xValues, incomeValues)
      }
    }
  }

  val lineProvider =
    LineCartesianLayer.LineProvider.series(
      LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(Fill(expenseColor))),
      LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(Fill(incomeColor))),
    )
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(lineProvider = lineProvider),
        startAxis = VerticalAxis.rememberStart(valueFormatter = yuanAxisFormatter()),
        bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = dateAxisFormatter(points.map { it.date })),
      ),
    modelProducer = modelProducer,
    modifier = Modifier.fillMaxWidth().height(220.dp),
  )
}

@Composable
private fun ChannelComparisonChart(
  items: List<ChannelAmountBreakdown>,
  expenseColor: Color,
  incomeColor: Color,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  val xValues = remember(items) { items.indices.map { it.toDouble() } }
  val expenseValues = remember(items) { items.map { it.expenseCents.toYuanDouble() } }
  val incomeValues = remember(items) { items.map { it.incomeCents.toYuanDouble() } }
  LaunchedEffect(modelProducer, xValues, expenseValues, incomeValues) {
    modelProducer.runTransaction {
      columnSeries {
        series(xValues, expenseValues)
        series(xValues, incomeValues)
      }
    }
  }

  val columnProvider =
    ColumnCartesianLayer.ColumnProvider.series(
      rememberLineComponent(Fill(expenseColor), 12.dp),
      rememberLineComponent(Fill(incomeColor), 12.dp),
    )
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(columnProvider = columnProvider),
        startAxis = VerticalAxis.rememberStart(valueFormatter = yuanAxisFormatter()),
        bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = channelAxisFormatter(items.map { it.channel })),
      ),
    modelProducer = modelProducer,
    modifier = Modifier.fillMaxWidth().height(220.dp),
  )
}

@Composable
private fun BreakdownPieChart(
  items: List<AmountBreakdown>,
  colors: List<Color>,
) {
  val modelProducer = remember { PieChartModelProducer() }
  val values = remember(items) { items.map { it.amountCents.toYuanDouble() } }
  LaunchedEffect(modelProducer, values) {
    modelProducer.runTransaction {
      pieSeries { series(values) }
    }
  }
  val sliceProvider =
    PieChart.SliceProvider.series(colors.map { color -> PieChart.Slice(fill = Fill(color)) })
  PieChartHost(
    chart = rememberPieChart(sliceProvider = sliceProvider, spacing = 2.dp),
    modelProducer = modelProducer,
    modifier = Modifier.fillMaxWidth().height(190.dp),
  )
}

@Composable
private fun ChartLegend(items: List<LegendItem>) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    items.forEach { item ->
      LegendRow(label = item.label, color = item.color, amountCents = item.amountCents)
    }
  }
}

@Composable
private fun BreakdownLegend(items: List<AmountBreakdown>, colors: List<Color>) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    items.forEachIndexed { index, item ->
      LegendRow(label = item.label, color = colors[index % colors.size], amountCents = item.amountCents)
    }
  }
}

@Composable
private fun ChannelLegend(items: List<ChannelAmountBreakdown>) {
  Text(
    items.joinToString(" / ") { it.channel },
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun LegendRow(label: String, color: Color, amountCents: Long) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(modifier = Modifier.height(10.dp).width(10.dp).background(color = color, shape = MaterialTheme.shapes.small))
      Text(label, style = MaterialTheme.typography.bodyMedium)
    }
    Text("¥${amountCents.asYuanText()}", style = MaterialTheme.typography.bodyMedium, color = color)
  }
}

@Composable
private fun dashboardPalette(primary: Color, secondary: Color): List<Color> =
  listOf(
    primary,
    secondary,
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.tertiary,
    MaterialTheme.colorScheme.secondary,
    MaterialTheme.colorScheme.outline,
    MaterialTheme.colorScheme.primaryContainer,
    MaterialTheme.colorScheme.tertiaryContainer,
  )

private fun yuanAxisFormatter(): CartesianValueFormatter =
  CartesianValueFormatter { _, value, _ -> value.yuanAxisText() }

private fun dateAxisFormatter(dates: List<LocalDate>): CartesianValueFormatter =
  CartesianValueFormatter { _, value, _ ->
    dates.getOrNull(value.toInt())?.format(DateAxisFormatter).orEmpty().ifBlank { value.toInt().toString() }
  }

private fun channelAxisFormatter(channels: List<String>): CartesianValueFormatter =
  CartesianValueFormatter { _, value, _ ->
    channels.getOrNull(value.toInt()).orEmpty().take(4).ifBlank { value.toInt().toString() }
  }

private fun Long.toYuanDouble(): Double = this / 100.0

private fun Double.yuanAxisText(): String =
  if (this >= 1000.0) "${(this / 1000.0).trimmed(1)}k" else trimmed(0)

private fun Double.trimmed(decimals: Int): String =
  "%.${decimals}f".format(this).trimEnd('0').trimEnd('.').ifBlank { "0" }

private data class LegendItem(
  val label: String,
  val color: Color,
  val amountCents: Long,
)

@Composable
private fun SettingsPane(
  state: MainScreenUiState,
  viewModel: MainScreenViewModel,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val isImportedSettings = state.settings.isImportedFromBackup
  var exportEntries by rememberSaveable { mutableStateOf(true) }
  var exportSettings by rememberSaveable { mutableStateOf(true) }
  var exportStartDate by rememberSaveable { mutableStateOf(LedgerFilter().startDate) }
  var exportEndDate by rememberSaveable { mutableStateOf(LedgerFilter().endDate) }
  var pendingExportBytes by remember { mutableStateOf<ByteArray?>(null) }
  var pendingImportBytes by remember { mutableStateOf<ByteArray?>(null) }
  var showImportConfirm by remember { mutableStateOf(false) }

  val createDocumentLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
      val bytes = pendingExportBytes
      if (uri != null && bytes != null) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
              ?: error("无法写入导出文件")
          }
          .onSuccess { viewModel.setStatusMessage("导出完成") }
          .onFailure { viewModel.setStatusMessage(it.message ?: "导出失败") }
      }
      pendingExportBytes = null
    }
  val openDocumentLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
              ?: error("无法读取导入文件")
          }
          .onSuccess {
            pendingImportBytes = it
            showImportConfirm = true
          }
          .onFailure { viewModel.setStatusMessage(it.message ?: "导入文件读取失败") }
      }
    }

  LazyColumn(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item {
      Text(
        "Agent 使用 OpenAI-compatible Chat Completions：base_url 通常填到 /v1，例如 https://api.openai.com/v1。",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    item {
      OutlinedTextField(
        value = state.settings.appTitle,
        onValueChange = viewModel::updateAppTitle,
        label = { Text("界面标题") },
        placeholder = { Text("J4Ledger") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    item {
      OutlinedTextField(
        value = state.settings.baseUrl.maskIfImported(isImportedSettings),
        onValueChange = viewModel::updateBaseUrl,
        label = { Text("base_url") },
        placeholder = { Text("https://api.example.com/v1") },
        singleLine = true,
        enabled = !isImportedSettings,
        readOnly = isImportedSettings,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    item {
      OutlinedTextField(
        value = state.settings.apiKey.maskIfImported(isImportedSettings),
        onValueChange = viewModel::updateApiKey,
        label = { Text("api_key") },
        singleLine = true,
        enabled = !isImportedSettings,
        readOnly = isImportedSettings,
        visualTransformation = if (isImportedSettings) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
      )
    }
    item {
      OutlinedTextField(
        value = state.settings.modelName.maskIfImported(isImportedSettings),
        onValueChange = viewModel::updateModelName,
        label = { Text("model_name") },
        placeholder = { Text("gpt-4.1-mini") },
        singleLine = true,
        enabled = !isImportedSettings,
        readOnly = isImportedSettings,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth(),
      )
    }
    if (isImportedSettings) {
      item {
        Text("当前 Agent 配置来自导入文件，敏感字段已遮罩且不可编辑。需要修改时请重新导入配置，或先重置清空。", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    item {
      Text("显示模式", style = MaterialTheme.typography.labelLarge)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        AppThemeMode.entries.forEach { mode ->
          FilterChip(
            selected = state.settings.themeMode == mode,
            onClick = { viewModel.updateThemeMode(mode) },
            label = { Text(mode.label) },
          )
        }
      }
    }
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = viewModel::saveSettings, modifier = Modifier.weight(1f)) { Text("保存配置") }
        TextButton(onClick = viewModel::resetSettings, modifier = Modifier.weight(1f)) { Text("重置") }
      }
      Spacer(Modifier.height(12.dp))
      HorizontalDivider()
      Spacer(Modifier.height(12.dp))
      Text("接口需要支持 tool_calls。未配置或服务不可用时，应用会提示你检查配置，不会自动套用本地规则。", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    item {
      HorizontalDivider()
      Spacer(Modifier.height(12.dp))
      Text("数据备份", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = exportEntries, onCheckedChange = { exportEntries = it })
        Text("导出账单数据")
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = exportSettings, onCheckedChange = { exportSettings = it })
        Text("导出设置")
      }
    }
    if (exportEntries) {
      item {
        DateRangeSelector(
          filter = LedgerFilter(exportStartDate, exportEndDate),
          onRangeChange = {
            exportStartDate = it.startDate
            exportEndDate = it.endDate
          },
          title = "导出账单日期",
        )
      }
    }
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
          onClick = {
            runCatching {
                viewModel.exportBackup(
                  BackupOptions(
                    includeEntries = exportEntries,
                    includeSettings = exportSettings,
                    entryFilter = LedgerFilter(exportStartDate, exportEndDate),
                  )
                )
              }
              .onSuccess {
                pendingExportBytes = it
                createDocumentLauncher.launch("j4ledger-backup-${LocalDate.now()}.zip")
              }
              .onFailure { viewModel.setStatusMessage(it.message ?: "导出失败") }
          },
          modifier = Modifier.weight(1f),
        ) {
          Text("导出")
        }
        Button(
          onClick = { openDocumentLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
          modifier = Modifier.weight(1f),
        ) {
          Text("覆盖导入")
        }
      }
      Text("导入会解压并解密 ZIP 备份，覆盖文件中包含的原有设置或账单数据。", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }

  if (showImportConfirm) {
    AlertDialog(
      onDismissRequest = {
        showImportConfirm = false
        pendingImportBytes = null
      },
      title = { Text("确认覆盖导入") },
      text = { Text("导入会解压并解密 ZIP 备份，然后覆盖当前原始数据。若文件包含账单，会替换现有账单；若包含设置，会替换现有设置。") },
      confirmButton = {
        Button(
          onClick = {
            val bytes = pendingImportBytes
            if (bytes != null) {
              runCatching { viewModel.importBackup(bytes) }
                .onFailure { viewModel.setStatusMessage(it.message ?: "导入失败") }
            }
            pendingImportBytes = null
            showImportConfirm = false
          }
        ) {
          Text("覆盖导入")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            pendingImportBytes = null
            showImportConfirm = false
          }
        ) {
          Text("取消")
        }
      },
    )
  }
}

@Composable
private fun DateRangeSelector(
  filter: LedgerFilter,
  onRangeChange: (LedgerFilter) -> Unit,
  title: String,
) {
  var showDialog by rememberSaveable { mutableStateOf(false) }
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Text("${filter.startDate} 至 ${filter.endDate}", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Button(onClick = { showDialog = true }) { Text("选择") }
    }
  }

  if (showDialog) {
    DateRangeDialog(
      title = title,
      initialFilter = filter,
      onDismiss = { showDialog = false },
      onConfirm = {
        onRangeChange(it)
        showDialog = false
      },
    )
  }
}

@Composable
private fun DateRangeDialog(
  title: String,
  initialFilter: LedgerFilter,
  onDismiss: () -> Unit,
  onConfirm: (LedgerFilter) -> Unit,
) {
  var startDate by rememberSaveable(initialFilter.startDate) { mutableStateOf(initialFilter.startDate) }
  var endDate by rememberSaveable(initialFilter.endDate) { mutableStateOf(initialFilter.endDate) }
  var errorText by rememberSaveable { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickRangeRows(
          onSelect = {
            startDate = it.startDate
            endDate = it.endDate
            errorText = ""
          }
        )
        OutlinedTextField(
          value = startDate,
          onValueChange = { startDate = it },
          label = { Text("开始日期") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = endDate,
          onValueChange = { endDate = it },
          label = { Text("结束日期") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        if (errorText.isNotBlank()) Text(errorText, color = MaterialTheme.colorScheme.error)
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val start = runCatching { LocalDate.parse(startDate) }.getOrNull()
          val end = runCatching { LocalDate.parse(endDate) }.getOrNull()
          when {
            start == null || end == null -> errorText = "日期格式应为 yyyy-MM-dd"
            start.isAfter(end) -> errorText = "开始日期不能晚于结束日期"
            else -> onConfirm(LedgerFilter(startDate, endDate))
          }
        }
      ) {
        Text("确定")
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
  )
}

@Composable
private fun QuickRangeRows(onSelect: (LedgerFilter) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      QuickRangeButton("今天", QuickRange.Today, onSelect)
      QuickRangeButton("本周", QuickRange.Week, onSelect)
      QuickRangeButton("本月", QuickRange.Month, onSelect)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      QuickRangeButton("本季度", QuickRange.Quarter, onSelect)
      QuickRangeButton("年度", QuickRange.Year, onSelect)
    }
  }
}

@Composable
private fun QuickRangeButton(label: String, range: QuickRange, onSelect: (LedgerFilter) -> Unit) {
  Button(onClick = { onSelect(range.toFilter()) }) { Text(label) }
}

private fun String.maskIfImported(isImportedSettings: Boolean): String =
  if (isImportedSettings) "********" else this

private fun String.toOccurredAtMillisOrNull(): Long? =
  runCatching {
      LocalDateTime.parse(this.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    }
    .getOrNull()

private val DeepIncomeYellow = Color(0xFF9A6B00)
private val DateAxisFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd")

private enum class QuickRange {
  Today,
  Week,
  Month,
  Quarter,
  Year;

  fun toFilter(today: LocalDate = LocalDate.now()): LedgerFilter {
    val start =
      when (this) {
        Today -> today
        Week -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        Month -> today.withDayOfMonth(1)
        Quarter -> {
          val firstMonth = ((today.monthValue - 1) / 3) * 3 + 1
          LocalDate.of(today.year, firstMonth, 1)
        }
        Year -> today.withDayOfYear(1)
      }
    val end =
      when (this) {
        Today -> today
        Week -> today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        Month -> today.withDayOfMonth(today.lengthOfMonth())
        Quarter -> start.plusMonths(3).minusDays(1)
        Year -> today.withDayOfYear(today.lengthOfYear())
      }
    return LedgerFilter(start.toString(), end.toString())
  }
}

private enum class MainTab(val title: String, val icon: String) {
  Record("记账", "记"),
  Bills("账单", "账"),
  Dashboard("Dashboard", "图"),
  Settings("设置", "设"),
}
