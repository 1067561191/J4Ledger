package com.example.androidproject.data

data class WechatBillRecord(
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
) {
  val uniqueKey: String
    get() = "$transactionType-$counterpart-$product"
}

data class WechatBillPreviewState(
  val records: List<WechatBillRecord> = emptyList(),
  val statusOptions: List<String> = emptyList(),
  val selectedStatuses: Set<String> = emptySet(),
  val categoryMap: Map<String, String> = emptyMap(),
  val isClassifying: Boolean = false,
  val importProgress: Float = 0f,
  val isImporting: Boolean = false,
) {
  val filteredRecords: List<WechatBillRecord>
    get() = records.filter { it.status in selectedStatuses }
}
