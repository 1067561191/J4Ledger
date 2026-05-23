package io.github.cming0420.agenticledger.data

import android.util.Log
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

private const val TAG = "AgenticLedger"

data class ExpenseParseResult(
  val entries: List<LedgerEntry>,
  val message: String,
)

interface ExpenseAgent {
  suspend fun parseExpense(rawText: String, settings: AgentSettings): ExpenseParseResult

  suspend fun parseIncome(rawText: String, settings: AgentSettings): ExpenseParseResult

  suspend fun parseEntry(rawText: String, settings: AgentSettings): ExpenseParseResult

  suspend fun classifyBillCategories(
    records: List<BillRecord>,
    settings: AgentSettings,
    onProgress: (processed: Int, total: Int) -> Unit = { _, _ -> }
  ): Map<String, String>
}

class OpenAiCompatibleExpenseAgent(
  private val clock: Clock = Clock.systemDefaultZone(),
) : ExpenseAgent {
  override suspend fun parseExpense(rawText: String, settings: AgentSettings): ExpenseParseResult {
    val trimmed = rawText.trim()
    require(trimmed.isNotBlank()) { "请输入一笔消费" }
    require(settings.isConfigured()) { "请先在设置页填写 base_url、api_key 和 model_name，再进行记账。" }

    Log.d(TAG, "[Agent] 开始解析消费: text=$trimmed, endpoint=${settings.baseUrl}, model=${settings.modelName}")
    val startTime = System.currentTimeMillis()
    val remoteResult =
      runCatching { requestRemoteAgent(trimmed, settings, LedgerEntryType.Expense) }
        .getOrElse { error ->
          Log.e(TAG, "[Agent] 解析消费失败: ${error.message}", error)
          throw IllegalStateException("AI 服务暂时不可用，请检查配置、网络或服务状态后再试。")
        }
    val duration = System.currentTimeMillis() - startTime
    Log.d(TAG, "[Agent] 解析消费成功: 耗时=${duration}ms, type=${remoteResult.type}, amount=${remoteResult.amountCents}, category=${remoteResult.category}")
    return ExpenseParseResult(listOf(remoteResult), "已由 Agent 解析并记消费")
  }

  override suspend fun parseIncome(rawText: String, settings: AgentSettings): ExpenseParseResult {
    val trimmed = rawText.trim()
    require(trimmed.isNotBlank()) { "请输入一笔收入" }
    require(settings.isConfigured()) { "请先在设置页填写 base_url、api_key 和 model_name，再进行记账。" }

    Log.d(TAG, "[Agent] 开始解析收入: text=$trimmed, endpoint=${settings.baseUrl}, model=${settings.modelName}")
    val startTime = System.currentTimeMillis()
    val remoteResult =
      runCatching { requestRemoteAgent(trimmed, settings, LedgerEntryType.Income) }
        .getOrElse { error ->
          Log.e(TAG, "[Agent] 解析收入失败: ${error.message}", error)
          throw IllegalStateException("AI 服务暂时不可用，请检查配置、网络或服务状态后再试。")
        }
    val duration = System.currentTimeMillis() - startTime
    Log.d(TAG, "[Agent] 解析收入成功: 耗时=${duration}ms, type=${remoteResult.type}, amount=${remoteResult.amountCents}, category=${remoteResult.category}")
    return ExpenseParseResult(listOf(remoteResult), "已由 Agent 解析并记收入")
  }

  override suspend fun parseEntry(rawText: String, settings: AgentSettings): ExpenseParseResult {
    val trimmed = rawText.trim()
    require(trimmed.isNotBlank()) { "请输入消费或收入" }
    require(settings.isConfigured()) { "请先在设置页填写 base_url、api_key 和 model_name，再进行记账。" }

    Log.d(TAG, "[Agent] 开始解析账单: text=$trimmed, endpoint=${settings.baseUrl}, model=${settings.modelName}")
    val startTime = System.currentTimeMillis()
    val remoteResults =
      runCatching { requestRemoteAgentAutoType(trimmed, settings) }
        .getOrElse { error ->
          Log.e(TAG, "[Agent] 解析账单失败: ${error.message}", error)
          throw IllegalStateException("AI 服务暂时不可用，请检查配置、网络或服务状态后再试。")
        }
    val duration = System.currentTimeMillis() - startTime
    Log.d(TAG, "[Agent] 解析账单成功: 耗时=${duration}ms, count=${remoteResults.size}")
    remoteResults.forEachIndexed { index, entry ->
      Log.d(TAG, "[Agent] 账单[$index]: type=${entry.type}, amount=${entry.amountCents}, channel=${entry.channel}, category=${entry.category}, description=${entry.description}")
    }
    val count = remoteResults.size
    return ExpenseParseResult(remoteResults, "已记 $count 笔账")
  }

  private suspend fun requestRemoteAgent(rawText: String, settings: AgentSettings, type: LedgerEntryType): LedgerEntry =
    withContext(Dispatchers.IO) {
      val endpoint = settings.baseUrl.trimEnd('/').let { base ->
        if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
      }
      Log.d(TAG, "[Agent] 请求远程Agent: endpoint=$endpoint, type=$type")
      val startTime = System.currentTimeMillis()
      val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 40_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
      }

      val requestBody = buildChatCompletionBody(rawText, settings, type)
      Log.d(TAG, "[Agent] 请求体: $requestBody")
      OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
        writer.write(requestBody.toString())
      }

      val responseText =
        if (connection.responseCode in 200..299) {
          connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
          val errorText = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
          Log.e(TAG, "[Agent] 响应失败: HTTP ${connection.responseCode}, error=$errorText")
          error("HTTP ${connection.responseCode}: $errorText")
        }

      val duration = System.currentTimeMillis() - startTime
      Log.d(TAG, "[Agent] 响应成功: HTTP ${connection.responseCode}, 耗时=${duration}ms")
      Log.d(TAG, "[Agent] 响应内容: ${responseText.take(500)}")
      parseAgentResponse(rawText, responseText, type)
    }

  private suspend fun requestRemoteAgentAutoType(rawText: String, settings: AgentSettings): List<LedgerEntry> =
    withContext(Dispatchers.IO) {
      val endpoint = settings.baseUrl.trimEnd('/').let { base ->
        if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
      }
      Log.d(TAG, "[Agent] 请求远程Agent(AutoType): endpoint=$endpoint")
      val startTime = System.currentTimeMillis()
      val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 40_000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
      }

      val requestBody = buildChatCompletionBodyAutoType(rawText, settings)
      Log.d(TAG, "[Agent] 请求体: $requestBody")
      OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
        writer.write(requestBody.toString())
      }

      val responseText =
        if (connection.responseCode in 200..299) {
          connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } else {
          val errorText = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
          Log.e(TAG, "[Agent] 响应失败: HTTP ${connection.responseCode}, error=$errorText")
          error("HTTP ${connection.responseCode}: $errorText")
        }

      val duration = System.currentTimeMillis() - startTime
      Log.d(TAG, "[Agent] 响应成功: HTTP ${connection.responseCode}, 耗时=${duration}ms")
      Log.d(TAG, "[Agent] 响应内容: ${responseText.take(500)}")
      parseAgentResponseAutoType(rawText, responseText)
    }

  private fun buildChatCompletionBody(rawText: String, settings: AgentSettings, type: LedgerEntryType): JSONObject {
    val isIncome = type == LedgerEntryType.Income
    val toolName = if (isIncome) "record_income" else "record_expense"
    val taskText =
      if (isIncome) {
        """
        你是 ${settings.appTitle.ifBlank { "AgenticLedger" }} 的记账 Agent。你的任务是从中文收入描述中抽取一条收入记录。
        当前日期是 ${LocalDate.now(clock)}，当前时间是 ${LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"))}。
        必须调用 record_income 工具；如果信息不完整，也要根据文本给出最合理的单条收入。
        amount_yuan 必须是正数。channel 规范化为微信、支付宝、银行卡、现金或其他。
        category 表示收入类型，用工资、奖金、报销、理财、兼职、转账、红包、其他之一。
        occurred_date 用 yyyy-MM-dd；occurred_time 用 HH:mm。没有明确日期或时间时使用当前日期和当前时间。
        """.trimIndent()
      } else {
        """
        你是 ${settings.appTitle.ifBlank { "AgenticLedger" }} 的记账 Agent。你的任务是从中文消费描述中抽取一条支出记录。
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
      你是 ${settings.appTitle.ifBlank { "AgenticLedger" }} 的记账 Agent。你的任务是从中文描述中抽取收入或支出记录。
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

  override suspend fun classifyBillCategories(
    records: List<BillRecord>,
    settings: AgentSettings,
    onProgress: (processed: Int, total: Int) -> Unit
  ): Map<String, String> =
    withContext(Dispatchers.IO) {
      val uniqueKeys = records.map { it.uniqueKey }.distinct()
      if (uniqueKeys.isEmpty()) return@withContext emptyMap()

      Log.d(TAG, "[Classify] 开始分类: uniqueKeys=${uniqueKeys.size}条, endpoint=${settings.baseUrl}, model=${settings.modelName}")
      val startTime = System.currentTimeMillis()

      val endpoint = settings.baseUrl.trimEnd('/').let { base ->
        if (base.endsWith("/chat/completions")) base else "$base/chat/completions"
      }

      val categoriesText = BILL_CATEGORIES.joinToString("、")
      val result = mutableMapOf<String, String>()
      var consecutiveHttpErrors = 0
      val maxConsecutiveHttpErrors = 2
      var remainingKeys = uniqueKeys.toList()
      var attempt = 0

      while (remainingKeys.isNotEmpty()) {
        attempt++
        Log.d(TAG, "[Classify] 第${attempt}次请求: 剩余${remainingKeys.size}条待分类")
        onProgress(uniqueKeys.size - remainingKeys.size, uniqueKeys.size)

        val (responseText, error) = requestClassification(
          endpoint = endpoint,
          settings = settings,
          uniqueKeys = remainingKeys,
          categoriesText = categoriesText,
          attempt = attempt
        )

        if (error != null) {
          consecutiveHttpErrors++
          Log.e(TAG, "[Classify] HTTP请求失败: consecutiveErrors=$consecutiveHttpErrors, error=$error")
          if (consecutiveHttpErrors >= maxConsecutiveHttpErrors) {
            Log.e(TAG, "[Classify] 连续HTTP失败${maxConsecutiveHttpErrors}次，终止分类")
            throw IllegalStateException("AI 分类服务连续失败 $maxConsecutiveHttpErrors 次，请检查配置、网络或服务状态后再试。")
          }
          continue
        }

        consecutiveHttpErrors = 0
        val parsedResponse = parseClassificationResponse(responseText!!)
        Log.d(TAG, "[Classify] AI返回结果: ${parsedResponse.size}条")
        val validCategories = validateAndNormalizeClassification(parsedResponse, remainingKeys)
        val missingCount = remainingKeys.size - validCategories.size
        Log.d(TAG, "[Classify] 校验结果: 成功=${validCategories.size}条, 缺失/无效=${missingCount}条")
        result.putAll(validCategories)
        remainingKeys = remainingKeys.filter { it !in result }

        if (remainingKeys.isNotEmpty()) {
          Log.d(TAG, "[Classify] 需要补齐: 剩余${remainingKeys.size}条")
        }
      }

      val duration = System.currentTimeMillis() - startTime
      Log.d(TAG, "[Classify] 分类完成: 总计=${result.size}条, 耗时=${duration}ms")
      onProgress(uniqueKeys.size, uniqueKeys.size)
      result
    }

  private suspend fun requestClassification(
    endpoint: String,
    settings: AgentSettings,
    uniqueKeys: List<String>,
    categoriesText: String,
    attempt: Int,
  ): Pair<String?, String?> =
    withContext(Dispatchers.IO) {
      try {
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

          可选分类：$categoriesText

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

        Log.d(TAG, "[Classify] 第${attempt}次请求: endpoint=$endpoint, model=${settings.modelName}, records=${uniqueKeys.size}条")
        Log.d(TAG, "[Classify] 请求体: ${requestBody.toString().take(500)}")

        val startTime = System.currentTimeMillis()
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
          writer.write(requestBody.toString())
        }

        val responseText =
          if (connection.responseCode in 200..299) {
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
          } else {
            val errorText = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            Log.e(TAG, "[Classify] 响应失败: HTTP ${connection.responseCode}, error=$errorText")
            error("HTTP ${connection.responseCode}: $errorText")
          }

        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "[Classify] 响应成功: HTTP ${connection.responseCode}, 耗时=${duration}ms")
        Log.d(TAG, "[Classify] 响应内容: ${responseText.take(500)}")

        Pair(responseText, null)
      } catch (e: Exception) {
        Log.e(TAG, "[Classify] 请求异常: ${e.message}", e)
        Pair(null, e.message ?: "未知错误")
      }
    }

  private fun validateAndNormalizeClassification(
    parsedResponse: Map<String, String>,
    uniqueKeys: List<String>,
  ): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val invalidCategories = mutableListOf<String>()
    uniqueKeys.forEachIndexed { index, key ->
      val category = parsedResponse[index.toString()]
      if (category != null && category in BILL_CATEGORIES) {
        result[key] = category
      } else if (category != null) {
        invalidCategories.add("[$index]=$category")
      }
    }
    if (invalidCategories.isNotEmpty()) {
      Log.w(TAG, "[Classify] 无效分类: ${invalidCategories.joinToString(", ")}")
    }
    return result
  }

  private fun parseClassificationResponse(responseText: String): Map<String, String> {
    val root = JSONObject(responseText)
    val content = root.getJSONArray("choices")
      .getJSONObject(0)
      .getJSONObject("message")
      .getString("content")

    Log.d(TAG, "[Classify] AI原始响应: ${content.take(300)}")
    val jsonText = content.substringAfter("```json", content).substringBefore("```").trim()
    val parsed = JSONObject(jsonText)
    val result = mutableMapOf<String, String>()
    parsed.keys().forEach { key ->
      result[key] = parsed.getString(key)
    }
    Log.d(TAG, "[Classify] 解析结果: ${result.size}条")
    return result
  }

  private fun AgentSettings.isConfigured(): Boolean =
    baseUrl.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()
}
