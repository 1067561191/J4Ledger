package com.example.androidproject.data

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ExpenseParseResult(
  val entry: LedgerEntry,
  val message: String,
)

interface ExpenseAgent {
  suspend fun parseExpense(rawText: String, settings: AgentSettings): ExpenseParseResult

  suspend fun parseIncome(rawText: String, settings: AgentSettings): ExpenseParseResult
}

class OpenAiCompatibleExpenseAgent(
  private val clock: Clock = Clock.systemDefaultZone(),
) : ExpenseAgent {
  override suspend fun parseExpense(rawText: String, settings: AgentSettings): ExpenseParseResult {
    val trimmed = rawText.trim()
    require(trimmed.isNotBlank()) { "请输入一笔消费" }
    require(settings.isConfigured()) { "请先在设置页填写 base_url、api_key 和 model_name，再进行记账。" }

    val remoteResult =
      runCatching { requestRemoteAgent(trimmed, settings, LedgerEntryType.Expense) }
        .getOrElse { throw IllegalStateException("AI 服务暂时不可用，请检查配置、网络或服务状态后再试。") }
    return ExpenseParseResult(remoteResult, "已由 Agent 解析并记消费")
  }

  override suspend fun parseIncome(rawText: String, settings: AgentSettings): ExpenseParseResult {
    val trimmed = rawText.trim()
    require(trimmed.isNotBlank()) { "请输入一笔收入" }
    require(settings.isConfigured()) { "请先在设置页填写 base_url、api_key 和 model_name，再进行记账。" }

    val remoteResult =
      runCatching { requestRemoteAgent(trimmed, settings, LedgerEntryType.Income) }
        .getOrElse { throw IllegalStateException("AI 服务暂时不可用，请检查配置、网络或服务状态后再试。") }
    return ExpenseParseResult(remoteResult, "已由 Agent 解析并记收入")
  }

  private suspend fun requestRemoteAgent(rawText: String, settings: AgentSettings, type: LedgerEntryType): LedgerEntry =
    withContext(Dispatchers.IO) {
      val endpoint = settings.baseUrl.trimEnd('/').let { base ->
        if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
      }
      val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 40_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
      }

      OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
        writer.write(buildChatCompletionBody(rawText, settings, type).toString())
      }

      val responseText =
        if (connection.responseCode in 200..299) {
          connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
          val errorText = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
          error("HTTP ${connection.responseCode}: $errorText")
        }

      parseAgentResponse(rawText, responseText, type)
    }

  private fun buildChatCompletionBody(rawText: String, settings: AgentSettings, type: LedgerEntryType): JSONObject {
    val isIncome = type == LedgerEntryType.Income
    val toolName = if (isIncome) "record_income" else "record_expense"
    val taskText =
      if (isIncome) {
        """
        你是 ${settings.appTitle.ifBlank { "J4Ledger" }} 的记账 Agent。你的任务是从中文收入描述中抽取一条收入记录。
        当前日期是 ${LocalDate.now(clock)}，当前时间是 ${LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"))}。
        必须调用 record_income 工具；如果信息不完整，也要根据文本给出最合理的单条收入。
        amount_yuan 必须是正数。channel 规范化为微信、支付宝、银行卡、现金或其他。
        category 表示收入类型，用工资、奖金、报销、理财、兼职、转账、红包、其他之一。
        occurred_date 用 yyyy-MM-dd；occurred_time 用 HH:mm。没有明确日期或时间时使用当前日期和当前时间。
        """.trimIndent()
      } else {
        """
        你是 ${settings.appTitle.ifBlank { "J4Ledger" }} 的记账 Agent。你的任务是从中文消费描述中抽取一条支出记录。
        当前日期是 ${LocalDate.now(clock)}，当前时间是 ${LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"))}。
        必须调用 record_expense 工具；如果信息不完整，也要根据文本给出最合理的单条支出。
        amount_yuan 必须是正数。channel 规范化为微信、支付宝、现金、银行卡、信用卡或其他。
        category 用餐饮、交通、购物、娱乐、居家、医疗、教育、旅行、其他之一。
        occurred_date 用 yyyy-MM-dd；occurred_time 用 HH:mm。没有明确日期或时间时使用当前日期和当前时间。
        """.trimIndent()
      }

    return JSONObject()
      .put("model", settings.modelName)
      .put("temperature", 0.1)
      .put(
        "messages",
        JSONArray()
          .put(
            JSONObject()
              .put("role", "system")
              .put("content", taskText)
          )
          .put(JSONObject().put("role", "user").put("content", rawText)),
      )
      .put(
        "tools",
        JSONArray()
          .put(
            JSONObject()
              .put("type", "function")
              .put(
                "function",
                JSONObject()
                  .put("name", toolName)
                  .put("description", if (isIncome) "Create one ledger income entry from natural language." else "Create one ledger expense entry from natural language.")
                  .put(
                    "parameters",
                    JSONObject()
                      .put("type", "object")
                      .put(
                        "properties",
                        JSONObject()
                          .put("amount_yuan", JSONObject().put("type", "number").put("description", "金额，单位元"))
                          .put("channel", JSONObject().put("type", "string"))
                          .put("category", JSONObject().put("type", "string").put("description", if (isIncome) "收入类型" else "消费分类"))
                          .put("description", JSONObject().put("type", "string").put("description", if (isIncome) "收入事项" else "消费事项"))
                          .put("occurred_date", JSONObject().put("type", "string").put("description", "yyyy-MM-dd"))
                          .put("occurred_time", JSONObject().put("type", "string").put("description", "HH:mm"))
                      )
                      .put("required", JSONArray().put("amount_yuan").put("channel").put("category").put("description").put("occurred_date").put("occurred_time"))
                  )
              )
          )
      )
      .put("tool_choice", "auto")
  }

  private fun parseAgentResponse(rawText: String, responseText: String, type: LedgerEntryType): LedgerEntry {
    val root = JSONObject(responseText)
    val message = root.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
    val argumentsText =
      when {
        message.has("tool_calls") -> {
          message
            .getJSONArray("tool_calls")
            .getJSONObject(0)
            .getJSONObject("function")
            .getString("arguments")
        }
        else -> message.optString("content")
      }
    val jsonText = argumentsText.substringAfter("```json", argumentsText).substringBefore("```").trim()
    val parsed = JSONObject(jsonText)
    return buildEntry(
      rawText = rawText,
      type = type,
      amountYuan = parsed.optDouble("amount_yuan", Double.NaN),
      channel = parsed.optString("channel", "其他"),
      category = parsed.optString("category", "其他"),
      description = parsed.optString("description", rawText),
      occurredDate = parsed.optString("occurred_date", LocalDate.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE)),
      occurredTime = parsed.optString("occurred_time", LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"))),
    )
  }

  private fun buildEntry(
    rawText: String,
    type: LedgerEntryType,
    amountYuan: Double,
    channel: String,
    category: String,
    description: String,
    occurredDate: String,
    occurredTime: String,
  ): LedgerEntry {
    require(amountYuan.isFinite() && amountYuan > 0) { "金额必须大于 0" }
    val date = runCatching { LocalDate.parse(occurredDate) }.getOrDefault(LocalDate.now(clock))
    val time = runCatching { LocalTime.parse(occurredTime) }.getOrDefault(LocalTime.now(clock))
    val zoneId = ZoneId.systemDefault()
    val occurredAt =
      ZonedDateTime.of(date, time, zoneId).toInstant().toEpochMilli()
    return LedgerEntry(
      id = UUID.randomUUID().toString(),
      type = type,
      amountCents = amountYuan.toCents(),
      channel = channel.ifBlank { "其他" },
      category = category.ifBlank { "其他" },
      description = description.ifBlank { rawText },
      occurredAtMillis = occurredAt,
      rawText = rawText,
      createdAtMillis = clock.millis(),
    )
  }

  private fun AgentSettings.isConfigured(): Boolean =
    baseUrl.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()
}
