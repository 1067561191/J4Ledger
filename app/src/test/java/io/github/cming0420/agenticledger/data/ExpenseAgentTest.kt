package io.github.cming0420.agenticledger.data

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExpenseAgentTest {
  private val agent =
    OpenAiCompatibleExpenseAgent(
      clock = Clock.fixed(Instant.parse("2026-05-23T10:00:00Z"), ZoneId.of("Asia/Shanghai"))
    )

  @Test
  fun parseExpense_withoutSettings_returnsFriendlyConfigurationMessage() = runTest {
    val error = runCatching { agent.parseExpense("我刚刚用微信支付28元买咖啡", AgentSettings()) }.exceptionOrNull()

    assertTrue(error is IllegalArgumentException)
    assertEquals("请先在设置页填写 base_url、api_key 和 model_name，再进行记账。", error?.message)
  }

  @Test
  fun amountCents_formatsAsYuanText() {
    assertEquals("28.50", 2850L.asYuanText())
  }
}

