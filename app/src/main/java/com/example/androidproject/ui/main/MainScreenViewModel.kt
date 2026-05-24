package com.example.androidproject.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.AgentSettings
import com.example.androidproject.data.AlipayBillParser
import com.example.androidproject.data.AppThemeMode
import com.example.androidproject.data.BackupOptions
import com.example.androidproject.data.BillPreviewState
import com.example.androidproject.data.BillRecord
import com.example.androidproject.data.ExpenseAgent
import com.example.androidproject.data.LedgerEntry
import com.example.androidproject.data.LedgerEntryType
import com.example.androidproject.data.LedgerFilter
import com.example.androidproject.data.LedgerRepository
import com.example.androidproject.data.WechatBillParser
import com.example.androidproject.data.asYuanText
import com.example.androidproject.data.localDate
import com.example.androidproject.data.signedAmountCents
import com.example.androidproject.data.toCents
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainScreenViewModel(
  private val ledgerRepository: LedgerRepository,
  private val expenseAgent: ExpenseAgent,
) : ViewModel() {
  private val formState = MutableStateFlow(MainScreenFormState(
    settings = ledgerRepository.snapshot.value.settings,
    drawerExpanded = ledgerRepository.snapshot.value.settings.drawerExpanded
  ))

  private val _billPreview = MutableStateFlow<BillPreviewState?>(null)
  val billPreview: StateFlow<BillPreviewState?> = _billPreview

  val uiState: StateFlow<MainScreenUiState> =
    combine(ledgerRepository.snapshot, formState) { snapshot, form ->
        val filteredEntries = snapshot.entries.filterBy(form.filter)
        MainScreenUiState(
          entries = filteredEntries,
          appTitle = snapshot.settings.appTitle.ifBlank { "J4Ledger" },
          totalText = filteredEntries.sumOf { it.signedAmountCents() }.asYuanText(),
          dashboardStats = buildDashboardStats(filteredEntries, form.filter),
          transcript = form.transcript,
          statusMessage = form.statusMessage,
          isProcessing = form.isProcessing,
          filter = form.filter,
          settings = form.settings,
          drawerExpanded = form.drawerExpanded,
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
      formState.update { it.copy(isProcessing = true, statusMessage = "正在解析账单...") }
      val result =
        runCatching { expenseAgent.parseEntry(text, formState.value.settings) }
          .getOrElse { error ->
            formState.update { it.copy(isProcessing = false, statusMessage = error.message ?: "解析失败") }
            return@launch
      }
      result.entries.forEach { ledgerRepository.saveEntry(it) }
      val details = result.entries.joinToString("；") { "${it.description} ${it.amountCents.asYuanText()} 元" }
      formState.update {
        it.copy(
          transcript = "",
          isProcessing = false,
          statusMessage = "${result.message}：$details",
        )
      }
    }
  }

  fun parseWechatBillExcel(inputStream: InputStream) {
    viewModelScope.launch {
      _billPreview.update { BillPreviewState(isClassifying = true) }
      runCatching {
        val records = WechatBillParser.parse(inputStream)
        val statusOptions = records.map { it.status }.distinct()
        _billPreview.update {
          BillPreviewState(
            records = records,
            statusOptions = statusOptions,
            selectedStatuses = statusOptions.toSet(),
            isClassifying = true,
          )
        }
        val categoryMap = expenseAgent.classifyBillCategories(records, formState.value.settings)
        _billPreview.update { it?.copy(categoryMap = categoryMap, isClassifying = false) }
      }.onFailure { error ->
        _billPreview.update { null }
        formState.update { it.copy(statusMessage = "解析微信账单失败：${error.message}") }
      }
    }
  }

  fun parseAlipayBillCsv(inputStream: InputStream) {
    viewModelScope.launch {
      _billPreview.update { BillPreviewState(isClassifying = true) }
      runCatching {
        val records = AlipayBillParser.parse(inputStream)
        val statusOptions = records.map { it.status }.distinct()
        _billPreview.update {
          BillPreviewState(
            records = records,
            statusOptions = statusOptions,
            selectedStatuses = statusOptions.toSet(),
            isClassifying = true,
          )
        }
        val categoryMap = expenseAgent.classifyBillCategories(records, formState.value.settings)
        _billPreview.update { it?.copy(categoryMap = categoryMap, isClassifying = false) }
      }.onFailure { error ->
        _billPreview.update { null }
        formState.update { it.copy(statusMessage = "解析支付宝账单失败：${error.message}") }
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

  fun importSelectedBillRecords() {
    val preview = _billPreview.value ?: return
    val records = preview.filteredRecords
    if (records.isEmpty()) {
      formState.update { it.copy(statusMessage = "没有可导入的记录") }
      return
    }

    viewModelScope.launch {
      _billPreview.update { it?.copy(isImporting = true, importProgress = 0f) }
      val total = records.size
      records.forEachIndexed { index, record ->
        val category = preview.categoryMap[record.uniqueKey] ?: "其他"
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
        )
        ledgerRepository.saveEntry(entry)
        _billPreview.update { it?.copy(importProgress = (index + 1f) / total) }
      }
      _billPreview.update { null }
      formState.update { it.copy(statusMessage = "已成功导入 ${records.size} 条账单") }
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
  val entries: List<LedgerEntry> = emptyList(),
  val appTitle: String = "J4Ledger",
  val totalText: String = "0.00",
  val dashboardStats: DashboardStats = DashboardStats(),
  val transcript: String = "",
  val statusMessage: String = "请先配置 Agent，然后输入一笔消费或收入进行记账。",
  val isProcessing: Boolean = false,
  val filter: LedgerFilter = LedgerFilter(),
  val settings: AgentSettings = AgentSettings(),
  val drawerExpanded: Boolean = true,
)

private data class MainScreenFormState(
  val transcript: String = "",
  val statusMessage: String = "请先配置 Agent，然后输入一笔消费或收入进行记账。",
  val isProcessing: Boolean = false,
  val filter: LedgerFilter = LedgerFilter(),
  val settings: AgentSettings = AgentSettings(),
  val drawerExpanded: Boolean = true,
)
