package com.example.androidproject.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.data.AgentSettings
import com.example.androidproject.data.AppThemeMode
import com.example.androidproject.data.BackupOptions
import com.example.androidproject.data.ExpenseAgent
import com.example.androidproject.data.LedgerEntry
import com.example.androidproject.data.LedgerFilter
import com.example.androidproject.data.LedgerRepository
import com.example.androidproject.data.asYuanText
import com.example.androidproject.data.localDate
import com.example.androidproject.data.signedAmountCents
import java.time.LocalDate
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
  private val formState = MutableStateFlow(MainScreenFormState(settings = ledgerRepository.snapshot.value.settings))

  val uiState: StateFlow<MainScreenUiState> =
    combine(ledgerRepository.snapshot, formState) { snapshot, form ->
        val filteredEntries = snapshot.entries.filterBy(form.filter)
        MainScreenUiState(
          entries = filteredEntries,
          appTitle = snapshot.settings.appTitle.ifBlank { "J4Ledger" },
          totalText = filteredEntries.sumOf { it.signedAmountCents() }.asYuanText(),
          dashboardStats = buildDashboardStats(filteredEntries, form.filter),
          transcript = form.transcript,
          incomeTranscript = form.incomeTranscript,
          statusMessage = form.statusMessage,
          isProcessing = form.isProcessing,
          filter = form.filter,
          settings = form.settings,
        )
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState())

  fun updateTranscript(value: String) {
    formState.update { it.copy(transcript = value) }
  }

  fun updateIncomeTranscript(value: String) {
    formState.update { it.copy(incomeTranscript = value) }
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

  fun exportBackup(options: BackupOptions): ByteArray =
    ledgerRepository.exportBackup(options)

  fun importBackup(zipBytes: ByteArray) {
    ledgerRepository.importBackup(zipBytes)
    formState.update { it.copy(settings = ledgerRepository.snapshot.value.settings, statusMessage = "导入完成，原有对应数据已被覆盖") }
  }

  fun submitTranscript() {
    val text = formState.value.transcript.trim()
    if (text.isBlank()) {
      formState.update { it.copy(statusMessage = "请输入一笔消费") }
      return
    }

    viewModelScope.launch {
      formState.update { it.copy(isProcessing = true, statusMessage = "正在解析账单...") }
      val result =
        runCatching { expenseAgent.parseExpense(text, formState.value.settings) }
          .getOrElse { error ->
            formState.update { it.copy(isProcessing = false, statusMessage = error.message ?: "解析失败") }
            return@launch
      }
      ledgerRepository.saveEntry(result.entry)
      formState.update {
        it.copy(
          transcript = "",
          isProcessing = false,
          statusMessage = "${result.message}：${result.entry.description}，${result.entry.amountCents.asYuanText()} 元",
        )
      }
    }
  }

  fun submitIncomeTranscript() {
    val text = formState.value.incomeTranscript.trim()
    if (text.isBlank()) {
      formState.update { it.copy(statusMessage = "请输入一笔收入") }
      return
    }

    viewModelScope.launch {
      formState.update { it.copy(isProcessing = true, statusMessage = "正在解析收入...") }
      val result =
        runCatching { expenseAgent.parseIncome(text, formState.value.settings) }
          .getOrElse { error ->
            formState.update { it.copy(isProcessing = false, statusMessage = error.message ?: "解析失败") }
            return@launch
          }
      ledgerRepository.saveEntry(result.entry)
      formState.update {
        it.copy(
          incomeTranscript = "",
          isProcessing = false,
          statusMessage = "${result.message}：${result.entry.description}，${result.entry.amountCents.asYuanText()} 元",
        )
      }
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
  val incomeTranscript: String = "",
  val statusMessage: String = "请先配置 Agent，然后输入一笔消费或收入进行记账。",
  val isProcessing: Boolean = false,
  val filter: LedgerFilter = LedgerFilter(),
  val settings: AgentSettings = AgentSettings(),
)

private data class MainScreenFormState(
  val transcript: String = "",
  val incomeTranscript: String = "",
  val statusMessage: String = "请先配置 Agent，然后输入一笔消费或收入进行记账。",
  val isProcessing: Boolean = false,
  val filter: LedgerFilter = LedgerFilter(),
  val settings: AgentSettings = AgentSettings(),
)
