package io.github.cming0420.agenticledger.ui.main

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.cming0420.agenticledger.data.AgentSettings
import io.github.cming0420.agenticledger.data.AlipayBillParser
import io.github.cming0420.agenticledger.data.AppThemeMode
import io.github.cming0420.agenticledger.data.BackupOptions
import io.github.cming0420.agenticledger.data.BillFileType
import io.github.cming0420.agenticledger.data.BillImportStep
import io.github.cming0420.agenticledger.data.BillPreviewState
import io.github.cming0420.agenticledger.data.BillRecord
import io.github.cming0420.agenticledger.data.ExpenseAgent
import io.github.cming0420.agenticledger.data.LedgerEntry
import io.github.cming0420.agenticledger.data.LedgerEntryType
import io.github.cming0420.agenticledger.data.LedgerFilter
import io.github.cming0420.agenticledger.data.LedgerRepository
import io.github.cming0420.agenticledger.data.WechatBillParser
import io.github.cming0420.agenticledger.data.asYuanText
import io.github.cming0420.agenticledger.data.localDate
import io.github.cming0420.agenticledger.data.signedAmountCents
import io.github.cming0420.agenticledger.data.toCents
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainScreenViewModel(
  private val application: Application,
  private val ledgerRepository: LedgerRepository,
  private val expenseAgent: ExpenseAgent,
) : AndroidViewModel(application) {
  private val formState = MutableStateFlow(MainScreenFormState(
    settings = ledgerRepository.snapshot.value.settings,
    drawerExpanded = ledgerRepository.snapshot.value.settings.drawerExpanded
  ))

  private val _billPreview = MutableStateFlow<BillPreviewState?>(null)
  val billPreview: StateFlow<BillPreviewState?> = _billPreview

  val uiState: StateFlow<MainScreenUiState> =
    combine(ledgerRepository.snapshot, formState) { snapshot, form ->
        val allEntries = snapshot.entries
        val filteredEntries = allEntries.filterBy(form.filter)
        MainScreenUiState(
          allEntries = allEntries,
          entries = filteredEntries,
          appTitle = snapshot.settings.appTitle.ifBlank { "AgenticLedger" },
          totalText = filteredEntries.sumOf { it.signedAmountCents() }.asYuanText(),
          dashboardStats = buildDashboardStats(allEntries, form.filter, form.dashboardTimeRange, form.currentDate, form.isExpenseMode),
          transcript = form.transcript,
          statusMessage = form.statusMessage,
          isProcessing = form.isProcessing,
          filter = form.filter,
          settings = form.settings,
          drawerExpanded = form.drawerExpanded,
          dashboardTimeRange = form.dashboardTimeRange,
          isExpenseMode = form.isExpenseMode,
          currentDate = form.currentDate,
          showCategoryComparison = form.showCategoryComparison,
          showAllRankings = form.showAllRankings,
        )
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState())

  fun updateTranscript(value: String) {
    formState.update { it.copy(transcript = value) }
  }

  fun updateFilterStart(value: String) {
    formState.update { it.copy(filter = it.filter.copy(startDate = value)) }
  }

  fun updateFilterEnd(value: String) {
    formState.update { it.copy(filter = it.filter.copy(endDate = value)) }
  }

  fun updateFilter(filter: LedgerFilter) {
    formState.update { it.copy(filter = filter) }
  }

  fun setDashboardTimeRange(range: DashboardTimeRange) {
    formState.update {
      val today = LocalDate.now()
      val newFilter = when (range) {
        DashboardTimeRange.Day -> LedgerFilter(today.toString(), today.toString())
        DashboardTimeRange.Week -> {
          val weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
          LedgerFilter(weekStart.toString(), weekStart.plusDays(6).toString())
        }
        DashboardTimeRange.Month -> {
          val monthStart = today.withDayOfMonth(1)
          LedgerFilter(monthStart.toString(), today.withDayOfMonth(today.lengthOfMonth()).toString())
        }
      }
      it.copy(dashboardTimeRange = range, currentDate = today, filter = newFilter)
    }
  }

  fun navigatePeriod(delta: Int) {
    formState.update {
      val newDate = when (it.dashboardTimeRange) {
        DashboardTimeRange.Day -> it.currentDate.plusDays(delta.toLong())
        DashboardTimeRange.Week -> it.currentDate.plusWeeks(delta.toLong())
        DashboardTimeRange.Month -> it.currentDate.plusMonths(delta.toLong())
      }
      val newFilter = when (it.dashboardTimeRange) {
        DashboardTimeRange.Day -> LedgerFilter(newDate.toString(), newDate.toString())
        DashboardTimeRange.Week -> {
          val weekStart = newDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
          LedgerFilter(weekStart.toString(), weekStart.plusDays(6).toString())
        }
        DashboardTimeRange.Month -> {
          val ym = java.time.YearMonth.from(newDate)
          LedgerFilter(ym.atDay(1).toString(), ym.atEndOfMonth().toString())
        }
      }
      it.copy(currentDate = newDate, filter = newFilter)
    }
  }

  fun toggleExpenseMode() {
    formState.update { it.copy(isExpenseMode = !it.isExpenseMode) }
  }

  fun toggleCategoryComparison() {
    formState.update { it.copy(showCategoryComparison = !it.showCategoryComparison) }
  }

  fun toggleRankingsExpand() {
    formState.update { it.copy(showAllRankings = !it.showAllRankings) }
  }

  fun updateBaseUrl(value: String) {
    formState.update { it.copy(settings = it.settings.copy(baseUrl = value)) }
  }

  fun updateAppTitle(value: String) {
    formState.update { it.copy(settings = it.settings.copy(appTitle = value)) }
  }

  fun updateApiKey(value: String) {
    formState.update { it.copy(settings = it.settings.copy(apiKey = value)) }
  }

  fun updateModelName(value: String) {
    formState.update { it.copy(settings = it.settings.copy(modelName = value)) }
  }

  fun updateThemeMode(value: AppThemeMode) {
    formState.update { it.copy(settings = it.settings.copy(themeMode = value)) }
  }

  fun toggleDrawer() {
    val newValue = !formState.value.drawerExpanded
    formState.update { it.copy(drawerExpanded = newValue) }
    ledgerRepository.saveSettings(formState.value.settings.copy(drawerExpanded = newValue))
  }

  fun saveSettings() {
    ledgerRepository.saveSettings(formState.value.settings)
    formState.update { it.copy(statusMessage = "Agent 配置已保存") }
  }

  fun resetSettings() {
    val resetSettings =
      AgentSettings(
        appTitle = "",
        themeMode = formState.value.settings.themeMode,
      )
    ledgerRepository.saveSettings(resetSettings)
    formState.update { it.copy(settings = resetSettings, statusMessage = "Agent 配置已重置") }
  }

  fun setStatusMessage(value: String) {
    formState.update { it.copy(statusMessage = value) }
  }

  fun updateEntry(entry: LedgerEntry) {
    ledgerRepository.updateEntry(entry)
    formState.update { it.copy(statusMessage = "账单已更新") }
  }

  fun deleteEntry(id: String) {
    ledgerRepository.deleteEntry(id)
    formState.update { it.copy(statusMessage = "账单已删除") }
  }

  fun deleteEntries(ids: List<String>) {
    ledgerRepository.deleteEntries(ids)
    formState.update { it.copy(statusMessage = "已删除 ${ids.size} 条账单") }
  }

  fun exportBackup(options: BackupOptions): ByteArray =
    ledgerRepository.exportBackup(options)

  fun importBackup(zipBytes: ByteArray) {
    ledgerRepository.importBackup(zipBytes)
    formState.update { it.copy(settings = ledgerRepository.snapshot.value.settings, statusMessage = "导入完成，原有对应数据已被覆盖") }
  }

  fun submitTranscript() {
    val text = formState.value.transcript.trim()
    if (text.isBlank()) {
      formState.update { it.copy(statusMessage = "请输入消费或收入") }
      return
    }

    viewModelScope.launch {
      formState.update { it.copy(isProcessing = true, statusMessage = "正在连接 AI 服务...") }
      val startTime = System.currentTimeMillis()
      val result =
        runCatching {
          formState.update { it.copy(statusMessage = "正在解析账单，请稍候...") }
          expenseAgent.parseEntry(text, formState.value.settings)
        }
          .getOrElse { error ->
            val duration = System.currentTimeMillis() - startTime
            formState.update { it.copy(isProcessing = false, statusMessage = "解析失败（耗时${duration}ms）：${error.message}") }
            return@launch
          }
      val duration = System.currentTimeMillis() - startTime
      result.entries.forEach { ledgerRepository.saveEntry(it) }
      val details = result.entries.joinToString("；") { "${it.description} ${it.amountCents.asYuanText()} 元" }
      formState.update {
        it.copy(
          transcript = "",
          isProcessing = false,
          statusMessage = "${result.message}（耗时${duration}ms）：$details",
        )
      }
    }
  }

  fun startBillImport(uri: Uri, fileType: BillFileType) {
    viewModelScope.launch {
      val fileName = withContext(Dispatchers.IO) { getFileName(uri) }
      _billPreview.update {
        BillPreviewState(
          fileUri = uri,
          fileName = fileName,
          fileType = fileType,
          step = BillImportStep.CONFIRM_FILE,
        )
      }
    }
  }

  private fun getFileName(uri: Uri): String {
    val cursor = application.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
      val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
      it.moveToFirst()
      if (nameIndex >= 0) it.getString(nameIndex) else "未知文件"
    } ?: "未知文件"
  }

  fun confirmAndLoadBillFile() {
    _billPreview.update { it?.copy(step = BillImportStep.LOADING_FILE) }
    loadBillFile()
  }

  private fun loadBillFile() {
    viewModelScope.launch {
      val preview = _billPreview.value ?: return@launch
      val uri = preview.fileUri ?: return@launch

      runCatching {
        // 在 IO 线程执行文件读取和解析
        val result = withContext(Dispatchers.IO) {
          val inputStream = application.contentResolver.openInputStream(uri)
            ?: error("无法打开文件")
          inputStream.use { stream ->
            when (preview.fileType) {
              BillFileType.WECHAT -> {
                val records = WechatBillParser.parse(stream)
                val statusOptions = records.map { it.status }.distinct()
                BillParseResult(records, statusOptions, emptyMap())
              }
              BillFileType.ALIPAY -> {
                val records = AlipayBillParser.parse(stream)
                val statusOptions = records.map { it.status }.distinct()
                val categoryMap = records.associate { it.uniqueKey to it.category.ifBlank { "其他" } }
                BillParseResult(records, statusOptions, categoryMap)
              }
            }
          }
        }

        // 回到主线程更新 UI
        _billPreview.update {
          it?.copy(
            records = result.records,
            statusOptions = result.statusOptions,
            selectedStatuses = result.statusOptions.toSet(),
            categoryMap = result.categoryMap,
            step = BillImportStep.DEDUPLICATING,
          )
        }
        deduplicateRecords()
      }.onFailure { error ->
        _billPreview.update {
          it?.copy(
            step = BillImportStep.COMPLETED,
            errorMessage = "加载文件失败：${error.message}",
          )
        }
      }
    }
  }

  private data class BillParseResult(
    val records: List<BillRecord>,
    val statusOptions: List<String>,
    val categoryMap: Map<String, String>,
  )

  private suspend fun deduplicateRecords() {
    val preview = _billPreview.value ?: return
    val records = preview.filteredRecords

    val transactionIds = records.map { it.transactionId }.filter { it.isNotBlank() }
    val existingIds = ledgerRepository.getExistingTransactionIds(transactionIds)
    val newRecords = records.filter { it.transactionId.isBlank() || it.transactionId !in existingIds }
    val duplicateCount = records.size - newRecords.size

    _billPreview.update {
      it?.copy(
        records = newRecords,
        duplicateCount = duplicateCount,
        step = when (preview.fileType) {
          BillFileType.ALIPAY -> BillImportStep.READY_TO_IMPORT
          BillFileType.WECHAT -> BillImportStep.CLASSIFYING
        },
      )
    }

    if (preview.fileType == BillFileType.WECHAT) {
      classifyWechatRecords()
    }
  }

  private suspend fun classifyWechatRecords() {
    val preview = _billPreview.value ?: return
    val records = preview.filteredRecords

    runCatching {
      val categoryMap = expenseAgent.classifyBillCategories(records, formState.value.settings) { processed, total ->
        _billPreview.update {
          it?.copy(
            classifyingProgress = processed,
            classifyingTotal = total,
          )
        }
      }
      _billPreview.update {
        it?.copy(
          categoryMap = categoryMap,
          step = BillImportStep.READY_TO_IMPORT,
        )
      }
    }.onFailure { error ->
      _billPreview.update {
        it?.copy(
          step = BillImportStep.COMPLETED,
          errorMessage = "AI 分类失败：${error.message}",
        )
      }
    }
  }

  fun toggleBillStatusFilter(status: String) {
    _billPreview.update { state ->
      state?.copy(
        selectedStatuses = if (status in state.selectedStatuses) {
          state.selectedStatuses - status
        } else {
          state.selectedStatuses + status
        }
      )
    }
  }

  fun confirmImport() {
    val preview = _billPreview.value ?: return
    val records = preview.filteredRecords
    if (records.isEmpty()) return

    viewModelScope.launch {
      _billPreview.update { it?.copy(step = BillImportStep.IMPORTING, importProgress = 0f) }

      val startTime = System.currentTimeMillis()
      val total = records.size
      var importedCount = 0

      records.forEachIndexed { index, record ->
        val category = preview.categoryMap[record.uniqueKey] ?: record.category.ifBlank { "其他" }
        val type = if (record.direction == "收入") LedgerEntryType.Income else LedgerEntryType.Expense
        val description = if (record.product.isNotBlank() && record.product != "/") record.product else record.transactionType
        val occurredAt = parseBillDateTime(record.transactionTime)

        val entry = LedgerEntry(
          id = UUID.randomUUID().toString(),
          type = type,
          amountCents = record.amount.toCents(),
          channel = record.paymentMethod,
          category = category,
          description = description,
          occurredAtMillis = occurredAt,
          rawText = "${record.transactionId} ${record.remark}".trim(),
          createdAtMillis = System.currentTimeMillis(),
          transactionId = record.transactionId,
        )
        ledgerRepository.saveEntry(entry)
        importedCount++
        _billPreview.update { it?.copy(importProgress = (index + 1f) / total) }
      }

      val duration = System.currentTimeMillis() - startTime
      _billPreview.update {
        it?.copy(
          step = BillImportStep.COMPLETED,
          importedCount = importedCount,
          importProgress = 1f,
        )
      }
    }
  }

  fun dismissBillPreview() {
    _billPreview.update { null }
  }

  private fun parseBillDateTime(dateTimeStr: String): Long {
    return try {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      val localDateTime = LocalDateTime.parse(dateTimeStr.trim(), formatter)
      localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
    } catch (_: Exception) {
      System.currentTimeMillis()
    }
  }

  private fun List<LedgerEntry>.filterBy(filter: LedgerFilter): List<LedgerEntry> {
    val start = runCatching { LocalDate.parse(filter.startDate) }.getOrNull()
    val end = runCatching { LocalDate.parse(filter.endDate) }.getOrNull()
    return filter { entry ->
      val date = entry.localDate()
      val afterStart = start == null || !date.isBefore(start)
      val beforeEnd = end == null || !date.isAfter(end)
      afterStart && beforeEnd
    }
  }
}

data class MainScreenUiState(
  val allEntries: List<LedgerEntry> = emptyList(),
  val entries: List<LedgerEntry> = emptyList(),
  val appTitle: String = "AgenticLedger",
  val totalText: String = "0.00",
  val dashboardStats: DashboardStats = DashboardStats(),
  val transcript: String = "",
  val statusMessage: String = "请先配置 Agent，然后输入一笔消费或收入进行记账。",
  val isProcessing: Boolean = false,
  val filter: LedgerFilter = LedgerFilter(),
  val settings: AgentSettings = AgentSettings(),
  val drawerExpanded: Boolean = true,
  val dashboardTimeRange: DashboardTimeRange = DashboardTimeRange.Month,
  val isExpenseMode: Boolean = true,
  val currentDate: LocalDate = LocalDate.now(),
  val showCategoryComparison: Boolean = false,
  val showAllRankings: Boolean = false,
)

private data class MainScreenFormState(
  val transcript: String = "",
  val statusMessage: String = "请先配置 Agent，然后输入一笔消费或收入进行记账。",
  val isProcessing: Boolean = false,
  val filter: LedgerFilter = LedgerFilter(),
  val settings: AgentSettings = AgentSettings(),
  val drawerExpanded: Boolean = true,
  val dashboardTimeRange: DashboardTimeRange = DashboardTimeRange.Month,
  val isExpenseMode: Boolean = true,
  val currentDate: LocalDate = LocalDate.now(),
  val showCategoryComparison: Boolean = false,
  val showAllRankings: Boolean = false,
)
