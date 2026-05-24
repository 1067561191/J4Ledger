package com.example.androidproject.ui.main

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidproject.data.AppThemeMode
import com.example.androidproject.data.BackupOptions
import com.example.androidproject.data.BillPreviewState
import com.example.androidproject.data.BillRecord
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
  val colorScheme = MaterialTheme.colorScheme

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = Color.Transparent,
    bottomBar = {
      NavigationBar(
        containerColor = colorScheme.surface,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0),
      ) {
        MainTab.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            icon = { Icon(tab.icon, contentDescription = null) },
            label = { Text(tab.title) },
            alwaysShowLabel = true,
          )
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                colorScheme.background,
                colorScheme.surfaceVariant.copy(alpha = 0.42f),
                colorScheme.background,
              )
            )
          )
          .padding(innerPadding),
    ) {
      ProvideVicoTheme(theme = rememberM3VicoTheme()) {
        AnimatedContent(
          targetState = selectedTab,
          label = "main-tab-transition",
          transitionSpec = {
            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
            (
              slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width ->
                direction * width / 5
              } +
                fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.98f, animationSpec = tween(300, easing = FastOutSlowInEasing))
              )
              .togetherWith(
                slideOutHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { width ->
                  -direction * width / 6
                } +
                  fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                  scaleOut(targetScale = 0.99f, animationSpec = tween(180, easing = FastOutSlowInEasing))
              )
              .using(SizeTransform(clip = false))
          },
          modifier = Modifier.fillMaxSize(),
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
private fun Header(state: MainScreenUiState, drawerExpanded: Boolean, onToggleDrawer: () -> Unit) {
  val stats = state.dashboardStats
  val incomeTotal = stats.dailySeries.sumOf { it.incomeCents }
  val expenseTotal = stats.dailySeries.sumOf { it.expenseCents }
  val isNegative = state.totalText.startsWith("-")
  val netColor = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
  ) {
    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable { onToggleDrawer() },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = if (drawerExpanded) "收起面板" else "展开面板",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
          Text(
            state.appTitle.ifBlank { "J4Ledger" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            "${state.filter.startDate} 至 ${state.filter.endDate}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("当前筛选净额", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
          netYuanText(state.totalText),
          style = MaterialTheme.typography.displaySmall,
          fontWeight = FontWeight.Bold,
          color = netColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun SummaryCard(state: MainScreenUiState) {
  val stats = state.dashboardStats
  val incomeTotal = stats.dailySeries.sumOf { it.incomeCents }
  val expenseTotal = stats.dailySeries.sumOf { it.expenseCents }

  Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
    SummaryMetric(
      label = "收入",
      value = "¥${incomeTotal.asYuanText()}",
      icon = Icons.AutoMirrored.Filled.TrendingUp,
      color = IncomeAccent,
      modifier = Modifier.weight(1f),
    )
    SummaryMetric(
      label = "支出",
      value = "¥${expenseTotal.asYuanText()}",
      icon = Icons.AutoMirrored.Filled.TrendingDown,
      color = MaterialTheme.colorScheme.error,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun SummaryMetric(
  label: String,
  value: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.heightIn(min = 64.dp),
    shape = AppCardShape,
    color = color.copy(alpha = 0.08f),
    border = BorderStroke(1.dp, color.copy(alpha = 0.14f)),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Text(
        value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun RecordPane(
  state: MainScreenUiState,
  viewModel: MainScreenViewModel,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
  ) {
    item {
      SectionHeader(title = "快速记账", subtitle = "自然语言输入，Agent 自动判断消费或收入，支持多笔")
    }
    item {
      EntryComposerCard(
        title = "记一笔账",
        subtitle = "消费或收入，一句话搞定，也可一次记多笔",
        value = state.transcript,
        onValueChange = viewModel::updateTranscript,
        label = "账单内容",
        placeholder = "例如：今天用微信支付 28 元买咖啡\n或：吃早饭5块，奶茶13块，工资到账3000",
        icon = Icons.Default.Payments,
        accentColor = MaterialTheme.colorScheme.primary,
        buttonText = "记账",
        onSubmit = viewModel::submitTranscript,
        isProcessing = state.isProcessing,
      )
    }
    item {
      StatusMessageCard(message = state.statusMessage, isProcessing = state.isProcessing)
    }
  }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
  Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    if (subtitle != null) {
      Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun EntryComposerCard(
  title: String,
  subtitle: String,
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String,
  icon: ImageVector,
  accentColor: Color,
  buttonText: String,
  onSubmit: () -> Unit,
  isProcessing: Boolean,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier.size(40.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 118.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        minLines = 4,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        PressableButton(
          text = buttonText,
          icon = Icons.Default.AddCircle,
          onClick = onSubmit,
          enabled = !isProcessing,
          containerColor = accentColor,
          modifier = Modifier.weight(1f),
        )
        if (isProcessing) {
          CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = accentColor)
        }
      }
    }
  }
}

@Composable
private fun StatusMessageCard(message: String, isProcessing: Boolean) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isProcessing) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
      } else {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
      }
      Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun PressableButton(
  text: String,
  icon: ImageVector,
  onClick: () -> Unit,
  enabled: Boolean,
  containerColor: Color,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.975f else 1f,
    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
    label = "button-press-scale",
  )

  Button(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    modifier = modifier.heightIn(min = 48.dp).scale(scale),
    shape = AppCardShape,
    colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = Color.White),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
  ) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(8.dp))
    Text(text, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun BillsPane(
  state: MainScreenUiState,
  viewModel: MainScreenViewModel,
  modifier: Modifier = Modifier,
) {
  var editingEntry by remember { mutableStateOf<LedgerEntry?>(null) }
  var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
  var showDeleteConfirm by remember { mutableStateOf(false) }
  val isMultiSelectMode = selectedIds.isNotEmpty()
  val entries = state.entries

  BackHandler(enabled = isMultiSelectMode) { selectedIds = emptySet() }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
      item { BillsSummaryCard(state = state) }
      item { DateRangeSelector(filter = state.filter, onRangeChange = viewModel::updateFilter, title = "账单日期") }
      item {
        BillsSectionHeader(
          entryCount = entries.size,
          isMultiSelectMode = isMultiSelectMode,
          selectedCount = selectedIds.size,
          allSelected = selectedIds.size == entries.size && entries.isNotEmpty(),
          onSelectAll = { selectedIds = entries.map { it.id }.toSet() },
          onDeselectAll = { selectedIds = emptySet() },
          onExitMultiSelect = { selectedIds = emptySet() },
        )
      }
      if (entries.isEmpty()) {
        item {
          EmptyStateCard(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            title = "这个日期范围内还没有账单",
            body = "记录一笔消费或收入后，这里会按时间展示明细。",
          )
        }
      } else {
        items(entries, key = { it.id }) { entry ->
          LedgerEntryCard(
            entry = entry,
            isSelected = entry.id in selectedIds,
            isMultiSelectMode = isMultiSelectMode,
            onClick = {
              if (isMultiSelectMode) {
                selectedIds = if (entry.id in selectedIds) selectedIds - entry.id else selectedIds + entry.id
              } else {
                editingEntry = entry
              }
            },
            onLongClick = {
              if (!isMultiSelectMode) {
                selectedIds = setOf(entry.id)
              }
            },
          )
        }
      }
    }

    AnimatedVisibility(
      visible = isMultiSelectMode,
      enter = slideInVertically(initialOffsetY = { it }),
      exit = slideOutVertically(targetOffsetY = { it }),
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedButton(onClick = { selectedIds = emptySet() }) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("退出")
          }
          Text(
            "已选 ${selectedIds.size} 条",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Button(
            onClick = { showDeleteConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("删除")
          }
        }
      }
    }
  }

  if (showDeleteConfirm && selectedIds.isNotEmpty()) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("确认删除") },
      text = { Text("确定删除选中的 ${selectedIds.size} 条账单？") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteEntries(selectedIds.toList())
            selectedIds = emptySet()
            showDeleteConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) { Text("删除") }
      },
      dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") } },
    )
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

@Composable
private fun BillsSectionHeader(
  entryCount: Int,
  isMultiSelectMode: Boolean,
  selectedCount: Int,
  allSelected: Boolean,
  onSelectAll: () -> Unit,
  onDeselectAll: () -> Unit,
  onExitMultiSelect: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        if (isMultiSelectMode) "已选 $selectedCount / $entryCount" else "账单明细",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      if (!isMultiSelectMode) {
        Text(
          "${entryCount} 条记录，单击编辑，长按多选",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    if (isMultiSelectMode) {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier
              .clip(AppCardShape)
              .clickable { if (allSelected) onDeselectAll() else onSelectAll() }
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Checkbox(
            checked = allSelected,
            onCheckedChange = { if (allSelected) onDeselectAll() else onSelectAll() },
          )
          Text("全选", style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onExitMultiSelect) {
          Icon(Icons.Default.Close, contentDescription = "退出多选")
        }
      }
    }
  }
}

@Composable
private fun BillsSummaryCard(state: MainScreenUiState) {
  val stats = state.dashboardStats
  val incomeTotal = stats.dailySeries.sumOf { it.incomeCents }
  val expenseTotal = stats.dailySeries.sumOf { it.expenseCents }
  val isNegative = state.totalText.startsWith("-")
  val netColor = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("当前筛选净额", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
          netYuanText(state.totalText),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = netColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      HorizontalDivider()
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryMetric(
          label = "收入",
          value = "¥${incomeTotal.asYuanText()}",
          icon = Icons.AutoMirrored.Filled.TrendingUp,
          color = IncomeAccent,
          modifier = Modifier.weight(1f),
        )
        SummaryMetric(
          label = "支出",
          value = "¥${expenseTotal.asYuanText()}",
          icon = Icons.AutoMirrored.Filled.TrendingDown,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LedgerEntryCard(
  entry: LedgerEntry,
  isSelected: Boolean,
  isMultiSelectMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
) {
  val isIncome = entry.type == LedgerEntryType.Income
  val amountPrefix = if (isIncome) "+¥" else "-¥"
  val amountColor = if (isIncome) IncomeAccent else MaterialTheme.colorScheme.error
  val typeIcon = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.99f else 1f,
    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
    label = "ledger-card-press-scale",
  )

  ElevatedCard(
    modifier =
      Modifier
        .fillMaxWidth()
        .scale(scale)
        .combinedClickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick,
          onLongClick = onLongClick,
        ),
    shape = AppCardShape,
    colors = CardDefaults.elevatedCardColors(
      containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
      else
        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        AnimatedVisibility(visible = isMultiSelectMode) {
          Checkbox(
            checked = isSelected,
            onCheckedChange = null,
          )
        }
        Box(
          modifier = Modifier.size(42.dp).clip(CircleShape).background(amountColor.copy(alpha = 0.1f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(imageVector = typeIcon, contentDescription = null, tint = amountColor, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            entry.description,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            "${entry.localDateTimeText()} · ${entry.type.label} · ${entry.channel} · ${entry.category}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Text(
          "$amountPrefix${entry.amountCents.asYuanText()}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = amountColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      if (entry.rawText.isNotBlank()) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = AppCardShape,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
          Text(
            entry.rawText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
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
  val incomeColor = IncomeAccent

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
  ) {
    item {
      DateRangeSelector(filter = state.filter, onRangeChange = viewModel::updateFilter, title = "统计日期")
    }
    item {
      DashboardMetrics(
        stats = stats,
        netText = state.totalText,
        entryCount = state.entries.size,
        expenseColor = expenseColor,
        incomeColor = incomeColor,
      )
    }
    if (!stats.hasAnyEntries) {
      item { EmptyDashboardCard() }
    } else {
      item {
        DashboardChartCard(
          title = "每日收支趋势",
          subtitle = "消费与收入的时间序列",
          icon = Icons.Default.BarChart,
          hasData = stats.dailySeries.isNotEmpty(),
        ) {
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
        DashboardChartCard(
          title = "消费分类占比",
          subtitle = "看清钱主要花在哪些地方",
          icon = Icons.Default.PieChart,
          hasData = stats.expenseByCategory.isNotEmpty(),
        ) {
          BreakdownPieChart(items = stats.expenseByCategory, colors = colors)
          BreakdownLegend(items = stats.expenseByCategory, colors = colors)
        }
      }
      item {
        val colors = dashboardPalette(incomeColor, expenseColor)
        DashboardChartCard(
          title = "收入分类占比",
          subtitle = "不同收入来源的结构",
          icon = Icons.Default.PieChart,
          hasData = stats.incomeByCategory.isNotEmpty(),
        ) {
          BreakdownPieChart(items = stats.incomeByCategory, colors = colors)
          BreakdownLegend(items = stats.incomeByCategory, colors = colors)
        }
      }
      item {
        DashboardChartCard(
          title = "渠道收支对比",
          subtitle = "按支付或收款渠道对比",
          icon = Icons.Default.QueryStats,
          hasData = stats.channelBreakdown.isNotEmpty(),
        ) {
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
        DashboardChartCard(
          title = "收入支出占比",
          subtitle = "本期资金流入与流出",
          icon = Icons.Default.AccountBalanceWallet,
          hasData = stats.incomeExpenseBreakdown.isNotEmpty(),
        ) {
          BreakdownPieChart(items = stats.incomeExpenseBreakdown, colors = colors)
          BreakdownLegend(items = stats.incomeExpenseBreakdown, colors = colors)
        }
      }
    }
  }
}

@Composable
private fun DashboardMetrics(
  stats: DashboardStats,
  netText: String,
  entryCount: Int,
  expenseColor: Color,
  incomeColor: Color,
) {
  val incomeTotal = stats.dailySeries.sumOf { it.incomeCents }
  val expenseTotal = stats.dailySeries.sumOf { it.expenseCents }
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      DashboardMetricCard(
        label = "收入",
        value = "¥${incomeTotal.asYuanText()}",
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        color = incomeColor,
        modifier = Modifier.weight(1f),
      )
      DashboardMetricCard(
        label = "支出",
        value = "¥${expenseTotal.asYuanText()}",
        icon = Icons.AutoMirrored.Filled.TrendingDown,
        color = expenseColor,
        modifier = Modifier.weight(1f),
      )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      DashboardMetricCard(
        label = "净额",
        value = netYuanText(netText),
        icon = Icons.Default.AccountBalanceWallet,
        color = if (netText.startsWith("-")) expenseColor else MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f),
      )
      DashboardMetricCard(
        label = "账单",
        value = "${entryCount} 条",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun DashboardMetricCard(
  label: String,
  value: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.heightIn(min = 86.dp),
    shape = AppCardShape,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Text(
        value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun EmptyDashboardCard() {
  EmptyStateCard(
    icon = Icons.Default.QueryStats,
    title = "暂无统计数据",
    body = "当前日期范围内还没有账单，图表会在有数据后自动呈现。",
  )
}

@Composable
private fun DashboardChartCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  hasData: Boolean,
  content: @Composable ColumnScope.() -> Unit,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
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
  ChartCanvas(height = 236.dp) {
    CartesianChartHost(
      chart =
        rememberCartesianChart(
          rememberLineCartesianLayer(lineProvider = lineProvider),
          startAxis = VerticalAxis.rememberStart(valueFormatter = yuanAxisFormatter()),
          bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = dateAxisFormatter(points.map { it.date })),
        ),
      modelProducer = modelProducer,
      modifier = Modifier.fillMaxSize(),
    )
  }
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
  ChartCanvas(height = 236.dp) {
    CartesianChartHost(
      chart =
        rememberCartesianChart(
          rememberColumnCartesianLayer(columnProvider = columnProvider),
          startAxis = VerticalAxis.rememberStart(valueFormatter = yuanAxisFormatter()),
          bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = channelAxisFormatter(items.map { it.channel })),
        ),
      modelProducer = modelProducer,
      modifier = Modifier.fillMaxSize(),
    )
  }
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
  ChartCanvas(height = 210.dp) {
    PieChartHost(
      chart = rememberPieChart(sliceProvider = sliceProvider, spacing = 2.dp),
      modelProducer = modelProducer,
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun ChartCanvas(
  height: androidx.compose.ui.unit.Dp,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().height(height),
    shape = AppCardShape,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
  ) {
    Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
      content()
    }
  }
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
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(modifier = Modifier.size(10.dp).background(color = color, shape = CircleShape))
        Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
      Text("¥${amountCents.asYuanText()}", style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
private fun dashboardPalette(primary: Color, secondary: Color): List<Color> =
  listOf(
    primary,
    secondary,
    Color(0xFF2563EB),
    Color(0xFF14B8A6),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6),
    Color(0xFF64748B),
    Color(0xFFDB2777),
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
  var showConfigMissingDialog by remember { mutableStateOf(false) }
  val billPreview by viewModel.billPreview.collectAsStateWithLifecycle()

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
  val wechatBillLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { viewModel.parseWechatBillExcel(it) }
              ?: error("无法读取微信账单文件")
          }
          .onFailure { viewModel.setStatusMessage(it.message ?: "微信账单读取失败") }
      }
    }
  val alipayBillLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { viewModel.parseAlipayBillCsv(it) }
              ?: error("无法读取支付宝账单文件")
          }
          .onFailure { viewModel.setStatusMessage(it.message ?: "支付宝账单读取失败") }
      }
    }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
  ) {
    item {
      SettingsSectionCard(title = "Agent 配置", icon = Icons.Default.Tune) {
        Text(
          "OpenAI-compatible Chat Completions：base_url 通常填到 /v1。",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
          value = state.settings.appTitle,
          onValueChange = viewModel::updateAppTitle,
          label = { Text("界面标题") },
          placeholder = { Text("J4Ledger") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
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
        if (isImportedSettings) {
          Text(
            "当前 Agent 配置来自导入文件，敏感字段已遮罩且不可编辑。需要修改时请重新导入配置，或先重置清空。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          PressableButton(
            text = "保存配置",
            icon = Icons.Default.Check,
            onClick = viewModel::saveSettings,
            enabled = true,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
          )
          OutlinedButton(
            onClick = viewModel::resetSettings,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            shape = AppCardShape,
          ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("重置")
          }
        }
        HorizontalDivider()
        Text(
          "接口需要支持 tool_calls。未配置或服务不可用时，应用会提示检查配置。",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    item {
      SettingsSectionCard(title = "外观", icon = Icons.Default.Settings) {
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
    }
    item {
      SettingsSectionCard(title = "数据备份", icon = Icons.Default.UploadFile) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = exportEntries, onCheckedChange = { exportEntries = it })
          Text("导出账单数据")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = exportSettings, onCheckedChange = { exportSettings = it })
          Text("导出设置")
        }
        if (exportEntries) {
          DateRangeSelector(
            filter = LedgerFilter(exportStartDate, exportEndDate),
            onRangeChange = {
              exportStartDate = it.startDate
              exportEndDate = it.endDate
            },
            title = "导出账单日期",
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          PressableButton(
            text = "导出",
            icon = Icons.Default.Download,
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
            enabled = true,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
          )
          PressableButton(
            text = "覆盖导入",
            icon = Icons.Default.UploadFile,
            onClick = { openDocumentLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
            enabled = true,
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
          )
        }
        Text(
          "导入会解压并解密 ZIP 备份，覆盖文件中包含的原有设置或账单数据。",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    item {
      SettingsSectionCard(title = "导入账单", icon = Icons.Default.UploadFile) {
        Text(
          "从微信或支付宝导出的账单文件中导入消费和收入记录。AI 将自动判断每笔账单的分类。",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val isAgentConfigured = state.settings.baseUrl.isNotBlank() && state.settings.apiKey.isNotBlank() && state.settings.modelName.isNotBlank()
        PressableButton(
          text = "选择微信账单文件 (.xlsx)",
          icon = Icons.Default.UploadFile,
          onClick = {
            if (!isAgentConfigured) {
              showConfigMissingDialog = true
            } else {
              wechatBillLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            }
          },
          enabled = true,
          containerColor = MaterialTheme.colorScheme.primary,
          modifier = Modifier.fillMaxWidth(),
        )
        PressableButton(
          text = "选择支付宝账单文件 (.csv)",
          icon = Icons.Default.UploadFile,
          onClick = {
            if (!isAgentConfigured) {
              showConfigMissingDialog = true
            } else {
              alipayBillLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/csv", "*/*"))
            }
          },
          enabled = true,
          containerColor = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }

  val preview = billPreview
  if (preview != null) {
    BillPreviewDialog(
      preview = preview,
      onToggleStatus = viewModel::toggleBillStatusFilter,
      onImport = viewModel::importSelectedBillRecords,
      onDismiss = viewModel::dismissBillPreview,
    )
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

  if (showConfigMissingDialog) {
    AlertDialog(
      onDismissRequest = { showConfigMissingDialog = false },
      title = { Text("Agent 配置缺失") },
      text = { Text("导入账单需要 AI 推理功能，请先在上方「Agent 配置」中填写 base_url、api_key 和 model_name。") },
      confirmButton = {
        Button(onClick = { showConfigMissingDialog = false }) {
          Text("我知道了")
        }
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BillPreviewDialog(
  preview: BillPreviewState,
  onToggleStatus: (String) -> Unit,
  onImport: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("导入账单") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (preview.isClassifying) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
          ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text("正在通过 AI 处理账单...")
          }
        } else {
          Text(
            "共解析 ${preview.records.size} 条记录，请选择要导入的状态：",
            style = MaterialTheme.typography.bodyMedium,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            preview.statusOptions.forEach { status ->
              FilterChip(
                selected = status in preview.selectedStatuses,
                onClick = { onToggleStatus(status) },
                label = {
                  val count = preview.records.count { it.status == status }
                  Text("$status ($count)")
                },
              )
            }
          }
          HorizontalDivider()
          Text(
            "已选择 ${preview.filteredRecords.size} 条记录",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
          )
          LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            items(preview.filteredRecords.take(50)) { record ->
              val category = preview.categoryMap[record.uniqueKey] ?: "分类中..."
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    record.counterpart,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    "${record.transactionType} · $category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Text(
                  "${if (record.direction == "收入") "+" else "-"}¥${String.format("%.2f", record.amount)}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold,
                  color = if (record.direction == "收入") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
              }
            }
            if (preview.filteredRecords.size > 50) {
              item {
                Text(
                  "... 还有 ${preview.filteredRecords.size - 50} 条记录",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
        if (preview.isImporting) {
          LinearProgressIndicator(
            progress = { preview.importProgress },
            modifier = Modifier.fillMaxWidth(),
          )
          Text(
            "导入中... ${(preview.importProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onImport,
        enabled = !preview.isClassifying && !preview.isImporting && preview.filteredRecords.isNotEmpty(),
      ) {
        Text("导入选中记录")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("取消")
      }
    },
  )
}

@Composable
private fun SettingsSectionCard(
  title: String,
  icon: ImageVector,
  content: @Composable ColumnScope.() -> Unit,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      }
      content()
    }
  }
}

@Composable
private fun DateRangeSelector(
  filter: LedgerFilter,
  onRangeChange: (LedgerFilter) -> Unit,
  title: String,
) {
  var showDialog by rememberSaveable { mutableStateOf(false) }
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
          "${filter.startDate} 至 ${filter.endDate}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      OutlinedButton(onClick = { showDialog = true }, shape = AppCardShape, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
        Text("选择")
      }
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
  FilterChip(selected = false, onClick = { onSelect(range.toFilter()) }, label = { Text(label) })
}

@Composable
private fun EmptyStateCard(
  icon: ImageVector,
  title: String,
  body: String,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(22.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
      }
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
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

private fun netYuanText(yuanText: String): String =
  if (yuanText.startsWith("-")) "-¥${yuanText.drop(1)}" else "¥$yuanText"

private val AppCardShape = RoundedCornerShape(8.dp)
private val IncomeAccent = Color(0xFF0F766E)
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

private enum class MainTab(val title: String, val icon: ImageVector) {
  Record("记账", Icons.Default.AddCircle),
  Bills("账单", Icons.AutoMirrored.Filled.ReceiptLong),
  Dashboard("统计", Icons.Default.QueryStats),
  Settings("设置", Icons.Default.Settings),
}
