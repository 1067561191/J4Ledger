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
  val entries: List<LedgerEntry>,
  val message: String,
)

interface ExpenseAgent {
  suspend fun parseExpense(rawText: String, settings: AgentSettings): ExpenseParseResult

  suspend fun parseIncome(rawText: String, settings: AgentSettings): ExpenseParseResult

  suspend fun parseEntry(rawText: String, settings: AgentSettings): ExpenseParseResult

  suspend fun classifyBillCategories(records: List<BillRecord>, settings: AgentSettings): Map<String, String>
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
    return ExpenseParseResult(listOf(remoteResult), "已由 Agent 解析并记消费")
  }

  override suspend fun parseIncome(rawText: String, settings: AgentSettings): ExpenseParseResult {
    val trimmed = rawText.trim()
    require(trimmed.isNotBlank()) { "请输入一笔收入" }
    require(settings.isConfigured()) { "请先在设置页填写 base_url、api_key 和 model_name，再进行记账。" }

    val remoteResult =
      runCatching { requestRemoteAgent(trimmed, settings, LedgerEntryType.Income) }
        .getOrElse { throw IllegalStateException("AI 服务暂时不可用，请检查配置、网络或服务状态后再试。") }
    return ExpenseParseResult(listOf(remoteResult), "已由 Agent 解析并记收入")
  }

  override suspend fun parseEntry(rawText: String, settings: AgentSettings): ExpenseParseResult {
    val trimmed = rawText.trim()
    require(trimmed.isNotBlank()) { "请输入消费或收入" }
    require(settings.isConfigured()) { "请先在设置页填写 base_url、api_key 和 model_name，再进行记账。" }

    val remoteResults =
      runCatching { requestRemoteAgentAutoType(trimmed, settings) }
        .getOrElse { throw IllegalStateException("AI 服务暂时不可用，请检查配置、网络或服务状态后再试。") }
    val count = remoteResults.size
    return ExpenseParseResult(remoteResults, "已记 $count 笔账")
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

  private suspend fun requestRemoteAgentAutoType(rawText: String, settings: AgentSettings): List<LedgerEntry> =
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
        writer.write(buildChatCompletionBodyAutoType(rawText, settings).toString())
      }

      val responseText =
        if (connection.responseCode in 200..299) {
          connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
          val errorText = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
          error("HTTP ${connection.responseCode}: $errorText")
        }

      parseAgentResponseAutoType(rawText, responseText)
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

  private fun buildChatCompletionBodyAutoType(rawText: String, settings: AgentSettings): JSONObject {
    val taskText =
      """
      你是 ${settings.appTitle.ifBlank { "J4Ledger" }} 的记账 Agent。你的任务是从中文描述中抽取收入或支出记录。
      当前日期是 ${LocalDate.now(clock)}，当前时间是 ${LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"))}。
      
      用户可能一次描述多笔账单，你需要为每笔账单分别调用 record_entry 工具。
      
      判断每笔账单是收入还是支出：
      - 收入关键词：工资、奖金、报销、理财收益、兼职收入、转账收款、红包收到、退款到账、分红、租金收入等
      - 支出关键词：买、购买、支付、消费、花费、吃饭、打车、购物、充值、缴费等
      
      每笔账单调用一次 record_entry 工具：
      - type 字段填 "income" 表示收入，"expense" 表示支出
      - amount_yuan 必须是正数
      - 如果是收入：channel 规范化为微信、支付宝、银行卡、现金或其他；category 用工资、奖金、报销、理财、兼职、转账、红包、其他之一
      - 如果是支出：channel 规范化为微信、支付宝、现金、银行卡、信用卡或其他；category 用餐饮、交通、购物、娱乐、居家、医疗、教育、旅行、其他之一
      - occurred_date 用 yyyy-MM-dd；occurred_time 用 HH:mm。没有明确日期或时间时使用当前日期和当前时间
      """.trimIndent()

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
                  .put("name", "record_entry")
                  .put("description", "Create one ledger entry (income or expense) from natural language.")
                  .put(
                    "parameters",
                    JSONObject()
                      .put("type", "object")
                      .put(
                        "properties",
                        JSONObject()
                          .put("type", JSONObject().put("type", "string").put("description", "income or expense").put("enum", JSONArray().put("income").put("expense")))
                          .put("amount_yuan", JSONObject().put("type", "number").put("description", "金额，单位元"))
                          .put("channel", JSONObject().put("type", "string"))
                          .put("category", JSONObject().put("type", "string").put("description", "收入类型或消费分类"))
                          .put("description", JSONObject().put("type", "string").put("description", "事项描述"))
                          .put("occurred_date", JSONObject().put("type", "string").put("description", "yyyy-MM-dd"))
                          .put("occurred_time", JSONObject().put("type", "string").put("description", "HH:mm"))
                      )
                      .put("required", JSONArray().put("type").put("amount_yuan").put("channel").put("category").put("description").put("occurred_date").put("occurred_time"))
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

  private fun parseAgentResponseAutoType(rawText: String, responseText: String): List<LedgerEntry> {
    val root = JSONObject(responseText)
    val message = root.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
    val entries = mutableListOf<LedgerEntry>()

    if (message.has("tool_calls")) {
      val toolCalls = message.getJSONArray("tool_calls")
      for (i in 0 until toolCalls.length()) {
        val argumentsText = toolCalls.getJSONObject(i).getJSONObject("function").getString("arguments")
        val jsonText = argumentsText.substringAfter("```json", argumentsText).substringBefore("```").trim()
        val parsed = JSONObject(jsonText)
        val typeStr = parsed.optString("type", "expense")
        val type = if (typeStr == "income") LedgerEntryType.Income else LedgerEntryType.Expense
        entries.add(
          buildEntry(
            rawText = rawText,
            type = type,
            amountYuan = parsed.optDouble("amount_yuan", Double.NaN),
            channel = parsed.optString("channel", "其他"),
            category = parsed.optString("category", "其他"),
            description = parsed.optString("description", rawText),
            occurredDate = parsed.optString("occurred_date", LocalDate.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE)),
            occurredTime = parsed.optString("occurred_time", LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"))),
          )
        )
      }
    } else {
      val content = message.optString("content")
      val jsonText = content.substringAfter("```json", content).substringBefore("```").trim()
      if (jsonText.isNotBlank()) {
        val parsed = JSONObject(jsonText)
        val typeStr = parsed.optString("type", "expense")
        val type = if (typeStr == "income") LedgerEntryType.Income else LedgerEntryType.Expense
        entries.add(
          buildEntry(
            rawText = rawText,
            type = type,
            amountYuan = parsed.optDouble("amount_yuan", Double.NaN),
            channel = parsed.optString("channel", "其他"),
            category = parsed.optString("category", "其他"),
            description = parsed.optString("description", rawText),
            occurredDate = parsed.optString("occurred_date", LocalDate.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE)),
            occurredTime = parsed.optString("occurred_time", LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"))),
          )
        )
      }
    }

    require(entries.isNotEmpty()) { "未能解析出任何账单" }
    return entries
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

  override suspend fun classifyBillCategories(records: List<BillRecord>, settings: AgentSettings): Map<String, String> =
    withContext(Dispatchers.IO) {
      val uniqueKeys = records.map { it.uniqueKey }.distinct()
      if (uniqueKeys.isEmpty()) return@withContext emptyMap()

      val endpoint = settings.baseUrl.trimEnd('/').let { base ->
        if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
      }

      val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 60_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
      }

      val taskText =
        """
        你是记账分类助手。根据账单记录的交易类型、交易对方和商品信息，判断每条记录的消费分类。

        可选分类：餐饮美食、服饰装扮、日用百货、家居家装、数码电器、运动户外、美容美发、母婴亲子、宠物、交通出行、爱车养车、住房物业、酒店旅游、文化休闲、教育培训、医疗健康、生活服务、公共服务、商业服务、公益捐赠、互助保障、投资理财、保险、信用借还、充值缴费、转账红包、亲友代付、账户存取、退款、其他

        用户会给你一个编号列表，每个编号对应一条账单记录。
        请为每条记录返回分类结果，格式为JSON对象，key为用户提供的编号（如0, 1, 2...），value为分类名称。
        示例：{"0": "餐饮美食", "1": "转账红包", "2": "日用百货"}
        """.trimIndent()

      val recordsText = uniqueKeys.mapIndexed { index, key ->
        val parts = key.split("|||")
        "$index. 交易类型: ${parts.getOrElse(0) { "" }}, 交易对方: ${parts.getOrElse(1) { "" }}, 商品: ${parts.getOrElse(2) { "" }}"
      }.joinToString("\n")

      val requestBody = JSONObject()
        .put("model", settings.modelName)
        .put("temperature", 0.1)
        .put(
          "messages",
          JSONArray()
            .put(JSONObject().put("role", "system").put("content", taskText))
            .put(JSONObject().put("role", "user").put("content", recordsText))
        )

      OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
        writer.write(requestBody.toString())
      }

      val responseText =
        if (connection.responseCode in 200..299) {
          connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
          val errorText = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
          error("HTTP ${connection.responseCode}: $errorText")
        }

      val parsedResponse = parseClassificationResponse(responseText)
      // 将数字索引转换回uniqueKey
      val result = mutableMapOf<String, String>()
      uniqueKeys.forEachIndexed { index, key ->
        val category = parsedResponse[index.toString()] ?: "其他"
        result[key] = category
      }
      result
    }

  private fun parseClassificationResponse(responseText: String): Map<String, String> {
    val root = JSONObject(responseText)
    val content = root.getJSONArray("choices")
      .getJSONObject(0)
      .getJSONObject("message")
      .getString("content")

    val jsonText = content.substringAfter("```json", content).substringBefore("```").trim()
    val parsed = JSONObject(jsonText)
    val result = mutableMapOf<String, String>()
    parsed.keys().forEach { key ->
      result[key] = parsed.getString(key)
    }
    return result
  }

  private fun AgentSettings.isConfigured(): Boolean =
    baseUrl.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()
}
