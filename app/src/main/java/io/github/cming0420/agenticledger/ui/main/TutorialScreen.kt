package io.github.cming0420.agenticledger.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.cming0420.agenticledger.R

data class TutorialStep(val text: String, val imageResId: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    type: String,
    onBack: () -> Unit,
) {
    val title = if (type == "wechat") "导出微信账单" else "导出支付宝账单"
    val steps = remember(type) {
        if (type == "wechat") wechatSteps() else alipaySteps()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(steps) { index, step ->
                TutorialStepCard(index = index + 1, step = step)
            }
        }
    }
}

@Composable
private fun TutorialStepCard(index: Int, step: TutorialStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Step $index",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                step.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                Image(
                    painter = painterResource(id = step.imageResId),
                    contentDescription = step.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.46f),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

private fun wechatSteps() = listOf(
    TutorialStep("【我】→【服务】", R.drawable.wechat_tutorial_1),
    TutorialStep("点击【钱包】", R.drawable.wechat_tutorial_2),
    TutorialStep("点击【账单】", R.drawable.wechat_tutorial_3),
    TutorialStep("点击右上角【三个点】", R.drawable.wechat_tutorial_4),
    TutorialStep("点击【下载账单】", R.drawable.wechat_tutorial_5),
    TutorialStep("选择【用于个人对账】", R.drawable.wechat_tutorial_6),
    TutorialStep("设置接收方式、账单时间、交易类型后点击【选好了】→【下一步】", R.drawable.wechat_tutorial_7),
    TutorialStep("打开微信 → 【微信支付】消息", R.drawable.wechat_tutorial_8),
    TutorialStep("点击【账单流水文件】→【查看详情】→ 用浏览器打开下载", R.drawable.wechat_tutorial_9),
)

private fun alipaySteps() = listOf(
    TutorialStep("【我的】→【账单】", R.drawable.alipay_tutorial_1),
    TutorialStep("点击右上角【三个点】", R.drawable.alipay_tutorial_2),
    TutorialStep("点击【开具交易流水证明】", R.drawable.alipay_tutorial_3),
    TutorialStep("选择【用于个人对账】→ 点击【申请】", R.drawable.alipay_tutorial_4),
    TutorialStep("设置交易类型、时间范围、展示交易对手信息、展示商品说明信息", R.drawable.alipay_tutorial_5),
    TutorialStep("选择【接收方式】", R.drawable.alipay_tutorial_6),
    TutorialStep("选择【支付宝】", R.drawable.alipay_tutorial_7),
    TutorialStep("【首页】→【最近消息】→【我的账单】", R.drawable.alipay_tutorial_8),
    TutorialStep("点击【查看详情】", R.drawable.alipay_tutorial_9),
    TutorialStep("点击【下载文件】", R.drawable.alipay_tutorial_10),
)
