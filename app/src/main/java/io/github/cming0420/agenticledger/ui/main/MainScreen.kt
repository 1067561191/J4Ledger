package io.github.cming0420.agenticledger.ui.main

import android.util.Log
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Leaderboard
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.cming0420.agenticledger.data.AppThemeMode
import io.github.cming0420.agenticledger.data.BackupOptions
import io.github.cming0420.agenticledger.data.BillFileType
import io.github.cming0420.agenticledger.data.BillImportStep
import io.github.cming0420.agenticledger.data.BillPreviewState
import io.github.cming0420.agenticledger.data.BillRecord
import io.github.cming0420.agenticledger.data.LedgerFilter
import io.github.cming0420.agenticledger.data.LedgerEntry
import io.github.cming0420.agenticledger.data.LedgerEntryType
import io.github.cming0420.agenticledger.data.OpenAiCompatibleExpenseAgent
import io.github.cming0420.agenticledger.data.SQLiteLedgerRepository
import io.github.cming0420.agenticledger.data.asYuanText
import io.github.cming0420.agenticledger.data.localDateTimeText
import io.github.cming0420.agenticledger.data.toCents
import io.github.cming0420.agenticledger.theme.AgenticLedgerTheme
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
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.abs
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private fun safeLog(tag: String, msg: String) {
  try { Log.d(tag, msg) } catch (_: Throwable) { }
}

@Composable
fun MainScreen(
  onNavigateToTutorial: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val application = context.applicationContext as android.app.Application
  val viewModel: MainScreenViewModel =
    viewModel { MainScreenViewModel(application, SQLiteLedgerRepository(context), OpenAiCompatibleExpenseAgent()) }
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  AgenticLedgerTheme(themeMode = state.settings.themeMode) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      MainScreenContent(state = state, viewModel = viewModel, onNavigateToTutorial = onNavigateToTutorial)
    }
  }
}

@Composable
private fun MainScreenContent(
  state: MainScreenUiState,
  viewModel: MainScreenViewModel,
  onNavigateToTutorial: (String) -> Unit = {},
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
              MainTab.Record -> RecordPane(state = state, viewModel = viewModel, onNavigateToTutorial = onNavigateToTutorial, modifier = Modifier.fillMaxSize())
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
            state.appTitle.ifBlank { "AgenticLedger" },
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
  onNavigateToTutorial: (String) -> Unit = {},
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
    item {
      BillImportCard(
        viewModel = viewModel,
        isAgentConfigured = state.settings.baseUrl.isNotBlank() && state.settings.apiKey.isNotBlank() && state.settings.modelName.isNotBlank(),
        onNavigateToTutorial = onNavigateToTutorial,
      )
    }
  }
}

@Composable
private fun BillImportCard(
  viewModel: MainScreenViewModel,
  isAgentConfigured: Boolean,
  onNavigateToTutorial: (String) -> Unit = {},
) {
  var showConfigMissingDialog by remember { mutableStateOf(false) }
  val billPreview by viewModel.billPreview.collectAsStateWithLifecycle()

  val wechatBillLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        viewModel.startBillImport(uri, BillFileType.WECHAT)
      }
    }
  val alipayBillLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        viewModel.startBillImport(uri, BillFileType.ALIPAY)
      }
    }

  SettingsSectionCard(title = "导入账单", icon = Icons.Default.UploadFile) {
    Text(
      "从微信或支付宝导出的账单文件中导入消费和收入记录。AI 将自动判断每笔账单的分类。",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
    HorizontalDivider()
    Text(
      "导出教程",
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PressableButton(
      text = "查看微信账单导出教程",
      icon = Icons.Default.Info,
      onClick = { onNavigateToTutorial("wechat") },
      enabled = true,
      containerColor = MaterialTheme.colorScheme.tertiary,
      modifier = Modifier.fillMaxWidth(),
    )
    PressableButton(
      text = "查看支付宝账单导出教程",
      icon = Icons.Default.Info,
      onClick = { onNavigateToTutorial("alipay") },
      enabled = true,
      containerColor = MaterialTheme.colorScheme.tertiary,
      modifier = Modifier.fillMaxWidth(),
    )
  }

  val preview = billPreview
  if (preview != null) {
    BillPreviewDialog(
      preview = preview,
      onToggleStatus = viewModel::toggleBillStatusFilter,
      onConfirmLoad = viewModel::confirmAndLoadBillFile,
      onConfirmImport = viewModel::confirmImport,
      onDismiss = viewModel::dismissBillPreview,
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
    // ── 时间范围 Tab（日/周/月） ──────────────────────────
    item {
      DashboardTimeRangeTabs(
        selected = state.dashboardTimeRange,
        onSelect = viewModel::setDashboardTimeRange,
      )
    }

    // ── 日期导航 + 收支切换 ───────────────────────────────
    item {
      DashboardHeader(
        timeRange = state.dashboardTimeRange,
        currentDate = state.currentDate,
        isExpenseMode = state.isExpenseMode,
        onNavigate = viewModel::navigatePeriod,
        onToggleExpenseMode = viewModel::toggleExpenseMode,
      )
    }

    // ── 月度视图 ─────────────────────────────────────────
    if (state.dashboardTimeRange == DashboardTimeRange.Month) {
      // 月度摘要卡片
      item {
        MonthlySummaryCard(
          summary = stats.monthlySummary,
          isExpenseMode = state.isExpenseMode,
          currentDate = state.currentDate,
          entryCount = state.entries.size,
          expenseColor = expenseColor,
          incomeColor = incomeColor,
        )
      }

      // 月度小结卡片：文字描述 + 柱状图 + 日历
      item {
        var calendarExpanded by rememberSaveable { mutableStateOf(false) }
        DashboardChartCard(
          title = "月度小结",
          subtitle = "近12个月收支趋势",
          icon = Icons.Default.BarChart,
          hasData = stats.monthlyBarSeries.isNotEmpty(),
        ) {
          // 金额变化文字描述
          if (stats.monthlySummary.changeDescription.isNotBlank()) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = AppCardShape,
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(
                  modifier = Modifier.size(6.dp).background(
                    MaterialTheme.colorScheme.primary, CircleShape
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  stats.monthlySummary.changeDescription,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.weight(1f),
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
          }

          // 12个月柱状图
          MonthlyBarChart(
            points = stats.monthlyBarSeries,
            expenseColor = expenseColor,
            averageCents = stats.monthlySummary.averageCents,
          )

          Spacer(modifier = Modifier.height(12.dp))

          // 日历（支持展开）
          DailyCalendarGrid(
            days = stats.dailyCalendar,
            isExpenseMode = state.isExpenseMode,
            expenseColor = expenseColor,
            incomeColor = incomeColor,
            isExpanded = calendarExpanded,
            onToggleExpand = { calendarExpanded = !calendarExpanded },
          )
        }
      }

      // 支出/收入分类卡片：描述 + 环状饼图 + 排行 + 对比
      item {
        val type = if (state.isExpenseMode) LedgerEntryType.Expense else LedgerEntryType.Income
        val colors = if (state.isExpenseMode) dashboardPalette(expenseColor, incomeColor)
        else dashboardPalette(incomeColor, expenseColor)
        val categories = if (state.isExpenseMode) stats.expenseByCategory else stats.incomeByCategory
        val typeLabel = if (state.isExpenseMode) "消费" else "收入"

        val topCategory = categories.firstOrNull()
        val catTotal = categories.sumOf { it.amountCents }
        val topPct = if (topCategory != null && catTotal > 0) topCategory.amountCents * 100.0 / catTotal else 0.0

        val biggestChange = stats.categoryComparison
          .filter { it.currentCents > 0 }
          .maxByOrNull { abs(it.changePercent) }

        DashboardChartCard(
          title = "${if (state.isExpenseMode) "支出" else "收入"}分类",
          subtitle = "查看分类占比",
          icon = Icons.Default.PieChart,
          hasData = categories.isNotEmpty(),
          trailing = {
            FilterChip(
              selected = state.showCategoryComparison,
              onClick = viewModel::toggleCategoryComparison,
              label = { Text("对比上月", style = MaterialTheme.typography.labelSmall) },
            )
          },
        ) {
          // 文字描述：占比最高 + 变化最大
          if (topCategory != null) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = AppCardShape,
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    "${topCategory.label} 分类${typeLabel}占比最高",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
                if (biggestChange != null && biggestChange.category != topCategory.label) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier.size(6.dp).background(
                        if (biggestChange.changePercent > 0) MaterialTheme.colorScheme.error
                        else Color(0xFF2E7D32),
                        CircleShape,
                      )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      "${biggestChange.category} 分类${typeLabel}较上月变化最大",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                  }
                }
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
          }

          // 环状饼图 + 中心文字
          Box(contentAlignment = Alignment.Center) {
            BreakdownPieChart(items = categories, colors = colors, innerSizeDp = 90.dp)
            if (topCategory != null) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  String.format("%.1f", topPct),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  "%",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // 饼图标签（带颜色圆点 + 百分比 + 金额）
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            categories.take(5).forEachIndexed { index, item ->
              val pct = if (catTotal > 0) item.amountCents * 100.0 / catTotal else 0.0
              val color = colors[index % colors.size]
              val comparison = stats.categoryComparison.find { it.category == item.label }
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppCardShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                  ) {
                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                    Text(item.label, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  }
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      "${String.format("%.1f", pct)}%",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                      "¥${item.amountCents.asYuanText()}",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
              }
            }
          }

          // 对比上月详情
          if (state.showCategoryComparison && stats.categoryComparison.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            val typeModeLabel = if (state.isExpenseMode) "支出" else "收入"
            Text(
              "分类排行分析",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            CategoryComparisonList(
              items = stats.categoryComparison,
              isExpenseMode = state.isExpenseMode,
            )
          }
        }
      }

      // 支付方式
      item {
        DashboardChartCard(
          title = "支付方式",
          subtitle = "按渠道对比",
          icon = Icons.Default.Payments,
          hasData = stats.paymentMethodBreakdown.isNotEmpty(),
        ) {
          PaymentMethodList(items = stats.paymentMethodBreakdown)
        }
      }

      // 同比趋势
      item {
        DashboardChartCard(
          title = "年度同比",
          subtitle = "今年 vs 去年同期",
          icon = Icons.Default.QueryStats,
          hasData = stats.yearlyYoY.currentSeries.isNotEmpty(),
        ) {
          YearlyYoYChart(
            data = stats.yearlyYoY,
            color = if (state.isExpenseMode) expenseColor else incomeColor,
          )
          val change = stats.yearlyYoY.totalChange
          TrendChangeRow(
            label = "同比变化",
            change = change,
            isExpenseMode = state.isExpenseMode,
          )
        }
      }
    }

    // ── 周度视图 ─────────────────────────────────────────
    if (state.dashboardTimeRange == DashboardTimeRange.Week) {
      // ① 周度摘要卡片
      item {
        WeeklySummaryCard(
          summary = stats.weeklySummary,
          isExpenseMode = state.isExpenseMode,
          currentDate = state.currentDate,
          entryCount = state.entries.size,
          expenseColor = expenseColor,
          incomeColor = incomeColor,
        )
      }
      // ② 一周小结双柱状图
      item {
        DashboardChartCard(
          title = "一周小结",
          subtitle = "本周 vs 上周每日收支对比",
          icon = Icons.Default.BarChart,
          hasData = stats.weeklyBarSeries.isNotEmpty(),
        ) {
          WeeklyBarChart(
            points = stats.weeklyBarSeries,
            expenseColor = if (state.isExpenseMode) expenseColor else incomeColor,
            incomeColor = incomeColor,
          )
        }
      }
      // ③ 支出/收入排行
      item {
        val rankingItems = stats.weeklyRanking
        DashboardChartCard(
          title = if (state.isExpenseMode) "支出排行" else "收入排行",
          subtitle = "本周按金额降序排列",
          icon = Icons.Default.Leaderboard,
          hasData = rankingItems.isNotEmpty(),
        ) {
          ExpenseRankingList(
            items = rankingItems,
            showAll = state.showAllRankings,
            onToggleExpand = viewModel::toggleRankingsExpand,
            expenseColor = if (state.isExpenseMode) expenseColor else incomeColor,
          )
        }
      }
      // ④ 分类饼图 + 对比上周 + 子分类详情
      item {
        val colors = if (state.isExpenseMode) dashboardPalette(expenseColor, incomeColor)
        else dashboardPalette(incomeColor, expenseColor)
        val categories = if (state.isExpenseMode) stats.expenseByCategory else stats.incomeByCategory
        DashboardChartCard(
          title = "${if (state.isExpenseMode) "支出" else "收入"}分类",
          subtitle = "本周分类占比",
          icon = Icons.Default.PieChart,
          hasData = categories.isNotEmpty(),
          trailing = {
            FilterChip(
              selected = state.showCategoryComparison,
              onClick = viewModel::toggleCategoryComparison,
              label = { Text("对比上周", style = MaterialTheme.typography.labelSmall) },
            )
          },
        ) {
          Box(contentAlignment = Alignment.Center) {
            BreakdownPieChart(items = categories, colors = colors)
            if (categories.isNotEmpty()) {
              val top = categories.first()
              val total = categories.sumOf { it.amountCents }
              val pct = if (total > 0) top.amountCents * 100 / total else 0
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  top.label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  "${pct}%",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          WeeklyCategoryDetailList(
            items = stats.weeklyCategoryDetails,
            colors = colors,
            isExpenseMode = state.isExpenseMode,
            showComparison = state.showCategoryComparison,
          )
        }
      }
    }

    // ── 日视图 ───────────────────────────────────────────
    if (state.dashboardTimeRange == DashboardTimeRange.Day) {
      // ① 摘要卡片：总额、笔数、较昨日变化
      item {
        DaySummaryCard(
          summary = stats.dailySummary,
          sevenDayComparison = stats.sevenDayComparison,
          isExpenseMode = state.isExpenseMode,
          currentDate = state.currentDate,
          entryCount = state.entries.size,
          expenseColor = expenseColor,
          incomeColor = incomeColor,
        )
      }
      // ② 7天环比柱状图
      item {
        DashboardChartCard(
          title = "今日对比7天前",
          subtitle = "7天环比趋势",
          icon = Icons.Default.BarChart,
          hasData = stats.sevenDayBarSeries.isNotEmpty(),
        ) {
          SevenDayBarChart(
            points = stats.sevenDayBarSeries,
            color = if (state.isExpenseMode) expenseColor else incomeColor,
          )
        }
      }
      // ③ 支出排行
      item {
        DashboardChartCard(
          title = if (state.isExpenseMode) "支出排行" else "收入排行",
          subtitle = "按金额降序排列",
          icon = Icons.Default.Leaderboard,
          hasData = stats.dailyRanking.isNotEmpty(),
        ) {
          ExpenseRankingList(
            items = stats.dailyRanking,
            showAll = state.showAllRankings,
            onToggleExpand = viewModel::toggleRankingsExpand,
            expenseColor = expenseColor,
          )
        }
      }
      // ④ 分类环图 + 对比7天前
      item {
        val colors = if (state.isExpenseMode) dashboardPalette(expenseColor, incomeColor)
        else dashboardPalette(incomeColor, expenseColor)
        val categories = if (state.isExpenseMode) stats.expenseByCategory else stats.incomeByCategory
        DashboardChartCard(
          title = "${if (state.isExpenseMode) "支出" else "收入"}分类",
          subtitle = "当日分类占比",
          icon = Icons.Default.PieChart,
          hasData = categories.isNotEmpty(),
          trailing = {
            FilterChip(
              selected = state.showCategoryComparison,
              onClick = viewModel::toggleCategoryComparison,
              label = { Text("对比7天前", style = MaterialTheme.typography.labelSmall) },
            )
          },
        ) {
          BreakdownPieChart(items = categories, colors = colors)
          BreakdownLegendWithComparison(
            comparisonItems = stats.dayCategoryComparison,
            colors = colors,
            isExpenseMode = state.isExpenseMode,
            showComparison = state.showCategoryComparison,
          )
        }
      }
    }
  }
}

// ── 时间范围 Tab ──────────────────────────────────────────

@Composable
private fun DashboardTimeRangeTabs(
  selected: DashboardTimeRange,
  onSelect: (DashboardTimeRange) -> Unit,
) {
  val selectedIndex = DashboardTimeRange.entries.indexOf(selected)
  SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
    DashboardTimeRange.entries.forEachIndexed { index, range ->
      SegmentedButton(
        shape = SegmentedButtonDefaults.itemShape(index = index, count = DashboardTimeRange.entries.size),
        onClick = { onSelect(range) },
        selected = index == selectedIndex,
        label = { Text(range.label) },
      )
    }
  }
}

// ── 日期导航 + 收支切换 ──────────────────────────────────

@Composable
private fun DashboardHeader(
  timeRange: DashboardTimeRange,
  currentDate: LocalDate,
  isExpenseMode: Boolean,
  onNavigate: (Int) -> Unit,
  onToggleExpenseMode: () -> Unit,
) {
  val dateText = when (timeRange) {
    DashboardTimeRange.Day -> "${currentDate.year}年${currentDate.monthValue}月${currentDate.dayOfMonth}日"
    DashboardTimeRange.Week -> {
      val weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
      val weekEnd = weekStart.plusDays(6)
      "${weekStart.monthValue}月${weekStart.dayOfMonth}日 - ${weekEnd.monthValue}月${weekEnd.dayOfMonth}日"
    }
    DashboardTimeRange.Month -> "${currentDate.year}年${currentDate.monthValue}月"
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = { onNavigate(-1) }) {
        Icon(Icons.Default.ChevronLeft, contentDescription = "上一${timeRange.label}")
      }
      Text(
        dateText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      IconButton(onClick = { onNavigate(1) }) {
        Icon(Icons.Default.ChevronRight, contentDescription = "下一${timeRange.label}")
      }
    }
    SingleChoiceSegmentedButtonRow {
      SegmentedButton(
        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        onClick = { if (!isExpenseMode) onToggleExpenseMode() },
        selected = isExpenseMode,
        label = { Text("支出") },
      )
      SegmentedButton(
        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        onClick = { if (isExpenseMode) onToggleExpenseMode() },
        selected = !isExpenseMode,
        label = { Text("收入") },
      )
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
  trailing: @Composable () -> Unit = {},
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
        trailing()
      }
      if (hasData) {
        content()
      } else {
        Text("暂无可统计数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

// ── 月度摘要卡片 ──────────────────────────────────────────

@Composable
private fun MonthlySummaryCard(
  summary: MonthlySummary,
  isExpenseMode: Boolean,
  currentDate: LocalDate,
  entryCount: Int,
  expenseColor: Color,
  incomeColor: Color,
) {
  val primaryColor = if (isExpenseMode) expenseColor else incomeColor
  val cardGradient = Brush.linearGradient(
    colors = listOf(
      primaryColor.copy(alpha = 0.18f),
      primaryColor.copy(alpha = 0.06f),
    ),
    start = androidx.compose.ui.geometry.Offset.Zero,
    end = androidx.compose.ui.geometry.Offset.Infinite,
  )
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    color = Color.Transparent,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(cardGradient, AppCardShape)
        .padding(horizontal = 20.dp, vertical = 18.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          if (isExpenseMode) "支出" else "收入",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = primaryColor.copy(alpha = 0.85f),
        )
        Text(
          "¥${summary.totalCents.asYuanText()}",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            "共${entryCount}笔",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          val change = summary.vsLastMonth
          if (change.amountCents != 0L) {
            val sign = if (change.amountCents > 0) "+" else ""
            val changeColor = if (isExpenseMode) {
              if (change.amountCents > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
            } else {
              if (change.amountCents > 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            }
            Text(
              "较上月 ${sign}${abs(change.amountCents).asYuanText()}元",
              style = MaterialTheme.typography.bodySmall,
              color = changeColor,
            )
          }
        }
      }
      Icon(
        imageVector = if (isExpenseMode) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
        contentDescription = null,
        tint = primaryColor.copy(alpha = 0.3f),
        modifier = Modifier.size(56.dp),
      )
    }
  }
}

// ── 月度柱状图（全 Canvas 绘制：柱顶金额、无Y轴、均值线）──

@Composable
private fun MonthlyBarChart(
  points: List<MonthlyBarPoint>,
  expenseColor: Color,
  averageCents: Long,
) {
  if (points.isEmpty()) return
  val maxCents = points.maxOf { it.expenseCents }.coerceAtLeast(1L)
  val barColor = Color(0xFF42A5F5)
  val averageColor = Color(0xFF64B5F6)
  val textColor = MaterialTheme.colorScheme.onSurface
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
  val textColorInt = textColor.toArgb()
  val labelColorInt = labelColor.toArgb()

  LaunchedEffect(points, maxCents, averageCents) {
    safeLog("MainScreen", "MonthlyBarChart: points=${points.size}, maxCents=$maxCents, avg=$averageCents")
    points.forEach { p ->
      val fraction = p.expenseCents.toFloat() / maxCents.toFloat()
      safeLog("MainScreen", "  ${p.yearMonth}: expense=${p.expenseCents}, fraction=${String.format("%.3f", fraction)}")
    }
  }

  val scrollState = rememberScrollState()
  val screenWidthDp = LocalConfiguration.current.screenWidthDp
  val barSlotDp = (screenWidthDp / 5f).dp
  val fullWidthDp = barSlotDp * points.size
  val canvasWidthDp = fullWidthDp.coerceAtLeast(screenWidthDp.dp)

  LaunchedEffect(Unit) {
    withFrameNanos {}
    scrollState.scrollTo(scrollState.maxValue)
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(top = 16.dp)
        .horizontalScroll(scrollState),
    ) {
      Canvas(modifier = Modifier.width(canvasWidthDp).fillMaxHeight()) {
        val chartH = size.height - 18.dp.toPx()
        val barCnt = points.size
        val spacing = size.width / barCnt
        val barW = (spacing * 0.45f).coerceAtMost(20.dp.toPx())

        // 均值虚线
        if (averageCents > 0) {
          val avgY = chartH * (1f - averageCents.toFloat() / maxCents.toFloat())
          val dash = 6.dp.toPx()
          val gap = 4.dp.toPx()
          var x = 0f
          while (x < size.width) {
            val endX = (x + dash).coerceAtMost(size.width)
            drawLine(averageColor, Offset(x, avgY), Offset(endX, avgY), strokeWidth = 1.5.dp.toPx())
            x += dash + gap
          }
        }

        // 柱子和文字（nativeCanvas 自由绘制，不受布局约束）
        val valuePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
          color = textColorInt
          textSize = 10.dp.toPx()
          textAlign = android.graphics.Paint.Align.CENTER
          isFakeBoldText = true
        }
        val monthPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
          color = labelColorInt
          textSize = 9.dp.toPx()
          textAlign = android.graphics.Paint.Align.CENTER
        }

        points.forEachIndexed { i, pt ->
          val barH = (pt.expenseCents.toFloat() / maxCents) * chartH * 0.85f
          val cx = i * spacing + spacing / 2f
          val barTop = chartH - barH

          // 柱子
          drawRect(barColor, Offset(cx - barW / 2f, barTop), Size(barW, barH))

          // 柱顶金额
          val yuanText = "¥${pt.expenseCents.asYuanText()}"
          drawContext.canvas.nativeCanvas.drawText(yuanText, cx, barTop - 4.dp.toPx(), valuePaint)

          // 月份标签
          drawContext.canvas.nativeCanvas.drawText("${pt.yearMonth.monthValue}月", cx, size.height - 2.dp.toPx(), monthPaint)
        }
      }
    }

    // 均值图例
    Surface(
      modifier = Modifier.padding(start = 4.dp).width(180.dp),
      shape = RoundedCornerShape(4.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      ) {
        Box(modifier = Modifier.width(14.dp).height(2.dp).background(averageColor))
        Text(
          "月支出均值 ¥${averageCents.asYuanText()}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

// ── 日历网格（支持展开/收起）──────────────────────────────

@Composable
private fun DailyCalendarGrid(
  days: List<DailyCalendarDay>,
  isExpenseMode: Boolean,
  expenseColor: Color,
  incomeColor: Color,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
) {
  val today = LocalDate.now()
  val primaryColor = if (isExpenseMode) expenseColor else incomeColor
  val headerDays = listOf("一", "二", "三", "四", "五", "六", "日")
  val firstDayOfWeek = days.firstOrNull()?.date?.dayOfWeek?.value ?: 1
  val emptyCells = firstDayOfWeek - 1
  val totalCells = emptyCells + days.size
  val totalRows = (totalCells + 6) / 7
  val visibleRows = if (isExpanded) totalRows else 2.coerceAtMost(totalRows)

  Column {
    Row(modifier = Modifier.fillMaxWidth()) {
      headerDays.forEach { day ->
        Text(
          day,
          modifier = Modifier.weight(1f),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    for (row in 0 until visibleRows) {
      Row(modifier = Modifier.fillMaxWidth()) {
        for (col in 1..7) {
          val index = row * 7 + col - emptyCells - 1
          if (index in days.indices) {
            val day = days[index]
            val amount = if (isExpenseMode) day.expenseCents else day.incomeCents
            Surface(
              modifier = Modifier.weight(1f).padding(1.dp),
              shape = RoundedCornerShape(4.dp),
              color = if (day.isToday) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
              border = if (day.isToday) BorderStroke(1.dp, primaryColor) else null,
            ) {
              Column(
                modifier = Modifier.padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  "${day.date.dayOfMonth}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                  color = if (day.isToday) primaryColor else MaterialTheme.colorScheme.onSurface,
                )
                if (amount > 0) {
                  Text(
                    "${amount / 100}",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor.copy(alpha = 0.7f),
                  )
                }
              }
            }
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
    if (totalRows > 2) {
      TextButton(
        onClick = onToggleExpand,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(
          if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isExpanded) "收起日历" else "展开全部")
      }
    }
  }
}

// ── 分类环比列表 ──────────────────────────────────────────

@Composable
private fun CategoryComparisonList(
  items: List<CategoryComparisonItem>,
  isExpenseMode: Boolean,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    items.forEach { item ->
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
          Text(item.category, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
          Column(horizontalAlignment = Alignment.End) {
            Text("¥${item.currentCents.asYuanText()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (item.previousCents > 0 || item.changeCents != 0L) {
              val sign = if (item.changeCents > 0) "+" else ""
              val color = if (isExpenseMode) {
                if (item.changeCents > 0) MaterialTheme.colorScheme.error else IncomeAccent
              } else {
                if (item.changeCents > 0) IncomeAccent else MaterialTheme.colorScheme.error
              }
              Text(
                "${sign}${String.format("%.1f", item.changePercent)}%",
                style = MaterialTheme.typography.labelSmall,
                color = color,
              )
            }
          }
        }
      }
    }
  }
}

// ── 支付方式列表 ──────────────────────────────────────────

@Composable
private fun PaymentMethodList(items: List<PaymentMethodItem>) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    items.forEach { item ->
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
          Column(modifier = Modifier.weight(1f)) {
            Text(item.method, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("${item.count}笔", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Column(horizontalAlignment = Alignment.End) {
            if (item.expenseCents > 0) {
              Text("支出 ¥${item.expenseCents.asYuanText()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            if (item.incomeCents > 0) {
              Text("收入 ¥${item.incomeCents.asYuanText()}", style = MaterialTheme.typography.bodySmall, color = IncomeAccent)
            }
          }
        }
      }
    }
  }
}

// ── 年度同比图表 ──────────────────────────────────────────

@Composable
private fun YearlyYoYChart(
  data: TrendComparisonData,
  color: Color,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  val xValues = remember(data) { data.currentSeries.indices.map { it.toDouble() } }
  val currentValues = remember(data) { data.currentSeries.map { it.amountCents.toYuanDouble() } }
  val previousValues = remember(data) { data.previousSeries.map { it.amountCents.toYuanDouble() } }
  LaunchedEffect(modelProducer, xValues, currentValues, previousValues) {
    modelProducer.runTransaction {
      lineSeries {
        series(xValues, currentValues)
        series(xValues, previousValues)
      }
    }
  }
  val lineProvider = LineCartesianLayer.LineProvider.series(
    LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(Fill(color))),
    LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(Fill(color.copy(alpha = 0.3f)))),
  )
  val labels = remember(data) { data.currentSeries.map { it.label } }
  ChartCanvas(height = 200.dp) {
    CartesianChartHost(
      chart = rememberCartesianChart(
        rememberLineCartesianLayer(lineProvider = lineProvider),
        startAxis = VerticalAxis.rememberStart(valueFormatter = yuanAxisFormatter()),
        bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = monthAxisFormatter(labels)),
      ),
      modelProducer = modelProducer,
      modifier = Modifier.fillMaxSize(),
    )
  }
  ChartLegend(
    items = listOf(
      LegendItem("今年", color, data.currentTotalCents),
      LegendItem("去年", color.copy(alpha = 0.3f), data.previousTotalCents),
    )
  )
}

// ── 趋势变化行 ───────────────────────────────────────────

@Composable
private fun TrendChangeRow(
  label: String,
  change: ChangeAmount,
  isExpenseMode: Boolean,
) {
  if (change.amountCents == 0L) return
  val sign = if (change.amountCents > 0) "+" else ""
  val color = if (isExpenseMode) {
    if (change.amountCents > 0) MaterialTheme.colorScheme.error else IncomeAccent
  } else {
    if (change.amountCents > 0) IncomeAccent else MaterialTheme.colorScheme.error
  }
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(label, style = MaterialTheme.typography.bodyMedium)
      Text(
        "${sign}¥${change.amountCents.asYuanText()} (${sign}${String.format("%.1f", change.percent)}%)",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
      )
    }
  }
}

// ── 周度摘要卡片 ──────────────────────────────────────────

@Composable
private fun WeeklySummaryCard(
  summary: WeeklySummary,
  isExpenseMode: Boolean,
  currentDate: LocalDate,
  entryCount: Int,
  expenseColor: Color,
  incomeColor: Color,
) {
  val primaryColor = if (isExpenseMode) expenseColor else incomeColor
  val totalCents = if (isExpenseMode) summary.thisWeekExpenseCents else summary.thisWeekIncomeCents
  val change = if (isExpenseMode) summary.expenseChange else summary.incomeChange
  val weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
  val cardGradient = Brush.linearGradient(listOf(primaryColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.5f)))
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    color = Color.Transparent,
  ) {
    Box(modifier = Modifier.background(cardGradient, AppCardShape)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val waveColor = Color.White.copy(alpha = 0.06f)
        val path = androidx.compose.ui.graphics.Path().apply {
          moveTo(size.width * 0.5f, size.height)
          cubicTo(size.width * 0.6f, size.height * 0.75f, size.width * 0.8f, size.height * 0.85f, size.width, size.height * 0.7f)
          lineTo(size.width, size.height)
          close()
        }
        drawPath(path, waveColor)
      }
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            if (isExpenseMode) "支出" else "收入",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
          )
          if (change.amountCents != 0L) {
            val direction = if (change.amountCents > 0) "↑" else "↓"
            val changeColor = if (isExpenseMode) {
              if (change.amountCents > 0) Color(0xFFFCA5A5) else Color(0xFF6EE7B7)
            } else {
              if (change.amountCents > 0) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
            }
            Text(
              "较上周 $direction${abs(change.amountCents).asYuanText()}元",
              color = changeColor,
              style = MaterialTheme.typography.labelSmall,
            )
          }
        }
        Text(
          "¥${totalCents.asYuanText()}",
          color = Color.White,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Bold,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            "共${entryCount}笔",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
          )
          Text(
            "查看统计设置 >",
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall,
          )
        }
      }
    }
  }
}



// ── 周度双柱状图 ──────────────────────────────────────────

@Composable
private fun WeeklyBarChart(
  points: List<WeeklyBarPoint>,
  expenseColor: Color,
  incomeColor: Color,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  val xValues = remember(points) { points.indices.map { it.toDouble() } }
  val thisWeekValues = remember(points) { points.map { it.thisWeekCents.toYuanDouble() } }
  val lastWeekValues = remember(points) { points.map { it.lastWeekCents.toYuanDouble() } }
  LaunchedEffect(modelProducer, xValues, thisWeekValues, lastWeekValues) {
    modelProducer.runTransaction {
      columnSeries {
        series(xValues, lastWeekValues)
        series(xValues, thisWeekValues)
      }
    }
  }
  val thisColor = expenseColor
  val lastColor = expenseColor.copy(alpha = 0.35f)
  val columnProvider = ColumnCartesianLayer.ColumnProvider.series(
    rememberLineComponent(Fill(lastColor), 8.dp),
    rememberLineComponent(Fill(thisColor), 8.dp),
  )
  val dayLabels = remember(points) { points.map { it.dayOfWeek } }
  ChartCanvas(height = 236.dp) {
    CartesianChartHost(
      chart = rememberCartesianChart(
        rememberColumnCartesianLayer(columnProvider = columnProvider),
        startAxis = VerticalAxis.rememberStart(valueFormatter = yuanAxisFormatter()),
        bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = monthAxisFormatter(dayLabels)),
      ),
      modelProducer = modelProducer,
      modifier = Modifier.fillMaxSize(),
    )
  }
  ChartLegend(
    items = listOf(
      LegendItem("上周", lastColor, points.sumOf { it.lastWeekCents }),
      LegendItem("当周", thisColor, points.sumOf { it.thisWeekCents }),
    )
  )
}

// ── 周度分类详情列表 ──────────────────────────────────────

@Composable
private fun WeeklyCategoryDetailList(
  items: List<WeeklyCategoryDetail>,
  colors: List<Color>,
  isExpenseMode: Boolean,
  showComparison: Boolean,
) {
  var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
  val totalCents = items.sumOf { it.currentCents }.toFloat()

  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    items.forEachIndexed { index, item ->
      val color = colors[index % colors.size]
      val percentage = if (totalCents > 0) item.currentCents / totalCents * 100f else 0f
      val isExpanded = item.category in expandedCategories

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            expandedCategories = if (isExpanded) expandedCategories - item.category
            else expandedCategories + item.category
          },
        shape = AppCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
              Text(item.category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                "${String.format("%.1f", percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  "¥${item.currentCents.asYuanText()}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                )
                Text(
                  "${item.count}笔",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
          if (showComparison && item.changeCents != 0L) {
            val sign = if (item.changeCents > 0) "+" else ""
            val changeColor = if (isExpenseMode) {
              if (item.changeCents > 0) MaterialTheme.colorScheme.error else IncomeAccent
            } else {
              if (item.changeCents > 0) IncomeAccent else MaterialTheme.colorScheme.error
            }
            Text(
              "较上周 ${sign}${abs(item.changeCents).asYuanText()}",
              style = MaterialTheme.typography.labelSmall,
              color = changeColor,
            )
          }
        }
      }

      if (isExpanded && item.subcategories.isNotEmpty()) {
        Column(
          modifier = Modifier.padding(start = 16.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          item.subcategories.forEach { sub ->
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = AppCardShape,
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(sub.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text(
                    "¥${sub.amountCents.asYuanText()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                  )
                  if (showComparison && sub.changeCents != 0L) {
                    val sign = if (sub.changeCents > 0) "+" else ""
                    val changeColor = if (isExpenseMode) {
                      if (sub.changeCents > 0) MaterialTheme.colorScheme.error else IncomeAccent
                    } else {
                      if (sub.changeCents > 0) IncomeAccent else MaterialTheme.colorScheme.error
                    }
                    Text(
                      "${sign}¥${abs(sub.changeCents).asYuanText()}",
                      style = MaterialTheme.typography.labelSmall,
                      color = changeColor,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
    if (items.isNotEmpty()) {
      Text(
        "~ 列表到底了 ~",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      )
    }
  }
}

// ── 日视图摘要卡片 ────────────────────────────────────────

@Composable
private fun DaySummaryCard(
  summary: DailySummary,
  sevenDayComparison: SevenDayComparison,
  isExpenseMode: Boolean,
  currentDate: LocalDate,
  entryCount: Int,
  expenseColor: Color,
  incomeColor: Color,
) {
  val primaryColor = if (isExpenseMode) expenseColor else incomeColor
  val gradient = Brush.linearGradient(listOf(primaryColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.5f)))
  val totalCents = if (isExpenseMode) summary.todayExpenseCents else summary.todayIncomeCents
  val change = if (isExpenseMode) summary.expenseChange else summary.incomeChange
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = AppCardShape,
    color = Color.Transparent,
  ) {
    Box(modifier = Modifier.background(gradient, AppCardShape).padding(20.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          "${currentDate.monthValue}月${currentDate.dayOfMonth}日 · ${if (isExpenseMode) "支出" else "收入"}",
          color = Color.White.copy(alpha = 0.85f),
          style = MaterialTheme.typography.titleSmall,
        )
        Text(
          "¥${totalCents.asYuanText()}",
          color = Color.White,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
          Text("共${entryCount}笔", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
          if (change.amountCents != 0L) {
            val sign = if (change.amountCents > 0) "+" else ""
            Text(
              "较昨日 ${sign}¥${change.amountCents.asYuanText()}",
              color = Color.White.copy(alpha = 0.8f),
              style = MaterialTheme.typography.bodySmall,
            )
          }
        }
      }
    }
  }
}

// ── 7天环比柱状图 ─────────────────────────────────────────

@Composable
private fun SevenDayBarChart(
  points: List<SevenDayBarPoint>,
  color: Color,
) {
  if (points.isEmpty()) return
  val max = points.maxOf { it.expenseCents }
  val axisColor = MaterialTheme.colorScheme.outline

  Box(modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 24.dp)) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val yAxisX = 0.dp.toPx()
      val xAxisY = size.height - 1.dp.toPx()
      drawLine(axisColor, Offset(yAxisX, 0f), Offset(yAxisX, xAxisY), strokeWidth = 1.dp.toPx())
      drawLine(axisColor, Offset(yAxisX, xAxisY), Offset(size.width, xAxisY), strokeWidth = 1.dp.toPx())
    }
    Row(
      modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 1.dp),
      horizontalArrangement = Arrangement.spacedBy(18.dp),
      verticalAlignment = Alignment.Bottom,
    ) {
      points.forEach { point ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Bottom,
        ) {
          Text(
            "¥${point.expenseCents.asYuanText()}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Spacer(modifier = Modifier.height(4.dp))
          val barHeight = if (max > 0) (point.expenseCents.toFloat() / max * 120).dp.coerceAtLeast(2.dp) else 2.dp
          Surface(
            modifier = Modifier.width(36.dp).height(barHeight),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            color = color,
          ) {}
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            point.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

// ── 支出排行列表 ──────────────────────────────────────────

@Composable
private fun ExpenseRankingList(
  items: List<ExpenseRankingItem>,
  showAll: Boolean,
  onToggleExpand: () -> Unit,
  expenseColor: Color,
) {
  val maxItems = 3
  val displayItems = if (showAll) items else items.take(maxItems)
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    displayItems.forEach { item ->
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = CircleShape,
            color = if (item.rank <= 3) expenseColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(28.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                "${item.rank}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.rank <= 3) expenseColor else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              item.description,
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              "${item.category} · ${item.channel}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Text(
            "¥${item.amountCents.asYuanText()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = expenseColor,
          )
        }
      }
    }
    if (items.size > maxItems) {
      TextButton(
        onClick = onToggleExpand,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(
          if (showAll) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (showAll) "收起" else "展开全部（共${items.size}笔）")
      }
    }
  }
}

// ── 月份轴格式化 ─────────────────────────────────────────

private fun monthAxisFormatter(labels: List<String>): CartesianValueFormatter =
  CartesianValueFormatter { _, value, _ -> labels.getOrElse(value.toInt()) { "" } }

// ── 已有图表组件 ──────────────────────────────────────────

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
  innerSizeDp: androidx.compose.ui.unit.Dp = 80.dp,
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
      chart = rememberPieChart(
        sliceProvider = sliceProvider,
        spacing = 2.dp,
        innerSize = PieSize.Inner.fixed(innerSizeDp),
      ),
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
private fun BreakdownLegendWithComparison(
  comparisonItems: List<CategoryComparisonItem>,
  colors: List<Color>,
  isExpenseMode: Boolean,
  showComparison: Boolean,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    comparisonItems.forEachIndexed { index, item ->
      val color = colors[index % colors.size]
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(10.dp),
          ) {}
          Text(item.category, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
          Column(horizontalAlignment = Alignment.End) {
            // 大字：今日金额
            Text("¥${item.currentCents.asYuanText()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            // 小字：差值（今日 - 7天前）
            if (showComparison && item.changeCents != 0L) {
              val sign = if (item.changeCents > 0) "+" else ""
              val changeColor = if (isExpenseMode) {
                if (item.changeCents > 0) MaterialTheme.colorScheme.error else IncomeAccent
              } else {
                if (item.changeCents > 0) IncomeAccent else MaterialTheme.colorScheme.error
              }
              Text(
                "${sign}${String.format("%.2f", item.changeCents / 100.0)}",
                style = MaterialTheme.typography.labelSmall,
                color = changeColor,
              )
            }
          }
        }
      }
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
          placeholder = { Text("AgenticLedger") },
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
                  createDocumentLauncher.launch("agenticledger-backup-${LocalDate.now()}.zip")
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
      SettingsSectionCard(title = "项目地址", icon = Icons.Default.Info) {
        val uriHandler = LocalUriHandler.current
        Text(
          text = "GitHub: AgenticLedger",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { uriHandler.openUri("https://github.com/1067561191/AgenticLedger") },
        )
      }
    }
    item {
      SettingsSectionCard(title = "鸣谢", icon = Icons.Default.Info) {
        val uriHandler = LocalUriHandler.current
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "@小米 Mimo大模型平台",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { uriHandler.openUri("https://platform.xiaomimimo.com/contact") },
          )
          Text(
            text = "@Opencode",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { uriHandler.openUri("https://opencode.ai/") },
          )
        }
      }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BillPreviewDialog(
  preview: BillPreviewState,
  onToggleStatus: (String) -> Unit,
  onConfirmLoad: () -> Unit,
  onConfirmImport: () -> Unit,
  onDismiss: () -> Unit,
) {
  var showCancelConfirm by rememberSaveable { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = { if (!preview.isBusy) onDismiss() },
    title = {
      Text(
        when (preview.step) {
          BillImportStep.CONFIRM_FILE -> "确认导入文件"
          BillImportStep.LOADING_FILE -> "加载账单文件"
          BillImportStep.DEDUPLICATING -> "检查重复记录"
          BillImportStep.CLASSIFYING -> "AI 智能分类"
          BillImportStep.READY_TO_IMPORT -> "确认导入"
          BillImportStep.IMPORTING -> "正在导入"
          BillImportStep.COMPLETED -> if (preview.errorMessage != null) "导入失败" else "导入完成"
        }
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (preview.step) {
          BillImportStep.CONFIRM_FILE -> {
            Text(
              "已选择文件：",
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              preview.fileName,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              when (preview.fileType) {
                BillFileType.WECHAT -> "微信账单文件将通过 AI 自动识别分类。"
                BillFileType.ALIPAY -> "支付宝账单文件将直接使用账单中的分类。"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              "是否导入该账单文件？",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }

          BillImportStep.LOADING_FILE -> {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
            ) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Text("正在读取账单文件...")
            }
          }

          BillImportStep.DEDUPLICATING -> {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
            ) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Text("正在检查重复记录...")
            }
          }

          BillImportStep.CLASSIFYING -> {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
            ) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Text("正在 AI 分类（${preview.classifyingProgress}/${preview.classifyingTotal}）...")
            }
            LinearProgressIndicator(
              progress = { if (preview.classifyingTotal > 0) preview.classifyingProgress.toFloat() / preview.classifyingTotal else 0f },
              modifier = Modifier.fillMaxWidth(),
            )
          }

          BillImportStep.READY_TO_IMPORT -> {
            Text(
              "识别到 ${preview.records.size + preview.duplicateCount} 条账目，其中 ${preview.duplicateCount} 条重复本次不计入。",
              style = MaterialTheme.typography.bodyMedium,
            )
            if (preview.records.isNotEmpty()) {
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
                "已选择 ${preview.filteredRecords.size} 条记录待导入",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
              LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                items(preview.filteredRecords.take(50)) { record ->
                  val category = preview.categoryMap[record.uniqueKey] ?: record.category.ifBlank { "其他" }
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
          }

          BillImportStep.IMPORTING -> {
            LinearProgressIndicator(
              progress = { preview.importProgress },
              modifier = Modifier.fillMaxWidth(),
            )
            Text(
              "正在导入 ${(preview.importProgress * 100).toInt()}%...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          BillImportStep.COMPLETED -> {
            if (preview.errorMessage != null) {
              Text(
                preview.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
              )
            } else {
              Text(
                "已成功导入 ${preview.importedCount} 条账单。",
                style = MaterialTheme.typography.bodyMedium,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      when (preview.step) {
        BillImportStep.CONFIRM_FILE -> {
          Button(onClick = onConfirmLoad) {
            Text("确认导入")
          }
        }
        BillImportStep.READY_TO_IMPORT -> {
          Button(
            onClick = onConfirmImport,
            enabled = preview.filteredRecords.isNotEmpty(),
          ) {
            Text("确认导入")
          }
        }
        BillImportStep.COMPLETED -> {
          Button(onClick = onDismiss) {
            Text("完成")
          }
        }
        else -> {}
      }
    },
    dismissButton = {
      when (preview.step) {
        BillImportStep.CONFIRM_FILE -> {
          TextButton(onClick = onDismiss) {
            Text("取消")
          }
        }
        BillImportStep.READY_TO_IMPORT -> {
          TextButton(onClick = onDismiss) {
            Text("取消")
          }
        }
        BillImportStep.LOADING_FILE, BillImportStep.DEDUPLICATING, BillImportStep.CLASSIFYING, BillImportStep.IMPORTING -> {
          TextButton(onClick = { showCancelConfirm = true }) {
            Text("取消导入")
          }
        }
        else -> {}
      }
    },
  )

  if (showCancelConfirm) {
    AlertDialog(
      onDismissRequest = { showCancelConfirm = false },
      title = { Text("确认取消") },
      text = { Text("当前正在处理账单，取消后已处理的数据不会回滚。确定要取消吗？") },
      confirmButton = {
        Button(
          onClick = {
            showCancelConfirm = false
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text("确定取消")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCancelConfirm = false }) {
          Text("继续处理")
        }
      },
    )
  }
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
