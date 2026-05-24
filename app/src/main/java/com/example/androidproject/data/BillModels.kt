package com.example.androidproject.data

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
) {
  val uniqueKey: String
    get() = "$transactionType|||$counterpart|||$product"
}

data class BillPreviewState(
  val records: List<BillRecord> = emptyList(),
  val statusOptions: List<String> = emptyList(),
  val selectedStatuses: Set<String> = emptySet(),
  val categoryMap: Map<String, String> = emptyMap(),
  val isClassifying: Boolean = false,
  val importProgress: Float = 0f,
  val isImporting: Boolean = false,
) {
  val filteredRecords: List<BillRecord>
    get() = records.filter { it.status in selectedStatuses }
}
