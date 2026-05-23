package io.github.cming0420.agenticledger.data

import android.net.Uri

val BILL_CATEGORIES = listOf(
  "餐饮美食", "服饰装扮", "日用百货", "家居家装", "数码电器",
  "运动户外", "美容美发", "母婴亲子", "宠物", "交通出行",
  "爱车养车", "住房物业", "酒店旅游", "文化休闲", "教育培训",
  "医疗健康", "生活服务", "公共服务", "商业服务", "公益捐赠",
  "互助保障", "投资理财", "保险", "信用借还", "充值缴费",
  "转账红包", "亲友代付", "账户存取", "退款", "其他"
)

enum class BillImportStep {
  CONFIRM_FILE,      // 确认文件
  LOADING_FILE,      // 正在加载文件
  DEDUPLICATING,     // 正在去重
  CLASSIFYING,       // 正在AI分类（仅微信）
  READY_TO_IMPORT,   // 准备导入（展示最终列表）
  IMPORTING,         // 正在导入
  COMPLETED,         // 导入完成
}

data class BillRecord(
  val transactionTime: String,
  val transactionType: String,
  val counterpart: String,
  val product: String,
  val direction: String,
  val amount: Double,
  val paymentMethod: String,
  val status: String,
  val transactionId: String,
  val remark: String,
  val category: String = "",
) {
  val uniqueKey: String
    get() = "$transactionType|||$counterpart|||$product"
}

data class BillPreviewState(
  val fileUri: Uri? = null,
  val fileName: String = "",
  val fileType: BillFileType = BillFileType.WECHAT,
  val step: BillImportStep = BillImportStep.CONFIRM_FILE,
  val records: List<BillRecord> = emptyList(),
  val statusOptions: List<String> = emptyList(),
  val selectedStatuses: Set<String> = emptySet(),
  val categoryMap: Map<String, String> = emptyMap(),
  val duplicateCount: Int = 0,
  val classifyingProgress: Int = 0,
  val classifyingTotal: Int = 0,
  val importProgress: Float = 0f,
  val importedCount: Int = 0,
  val errorMessage: String? = null,
) {
  val filteredRecords: List<BillRecord>
    get() = records.filter { it.status in selectedStatuses }

  val isBusy: Boolean
    get() = step == BillImportStep.LOADING_FILE ||
            step == BillImportStep.DEDUPLICATING ||
            step == BillImportStep.CLASSIFYING ||
            step == BillImportStep.IMPORTING
}

enum class BillFileType {
  WECHAT,
  ALIPAY
}
