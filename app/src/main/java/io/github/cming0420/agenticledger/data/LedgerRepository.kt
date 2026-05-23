package io.github.cming0420.agenticledger.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

interface LedgerRepository {
  val snapshot: StateFlow<LedgerSnapshot>

  fun saveEntry(entry: LedgerEntry)

  fun updateEntry(entry: LedgerEntry)

  fun deleteEntry(id: String)

  fun deleteEntries(ids: List<String>)

  fun saveSettings(settings: AgentSettings)

  fun exportBackup(options: BackupOptions): ByteArray

  fun importBackup(zipBytes: ByteArray)

  fun getExistingTransactionIds(ids: List<String>): Set<String>
}

class SQLiteLedgerRepository(context: Context) : LedgerRepository {
  private val dbHelper = LedgerDbHelper(context.applicationContext)

  private val _snapshot = MutableStateFlow(loadSnapshot())
  override val snapshot: StateFlow<LedgerSnapshot> = _snapshot.asStateFlow()

  override fun saveEntry(entry: LedgerEntry) {
    dbHelper.writableDatabase.insertWithOnConflict(TABLE_ENTRIES, null, entry.toValues(), SQLiteDatabase.CONFLICT_REPLACE)
    refresh()
  }

  override fun updateEntry(entry: LedgerEntry) {
    dbHelper.writableDatabase.update(TABLE_ENTRIES, entry.toValues(), "id = ?", arrayOf(entry.id))
    refresh()
  }

  override fun deleteEntry(id: String) {
    dbHelper.writableDatabase.delete(TABLE_ENTRIES, "id = ?", arrayOf(id))
    refresh()
  }

  override fun deleteEntries(ids: List<String>) {
    if (ids.isEmpty()) return
    val db = dbHelper.writableDatabase
    db.beginTransaction()
    try {
      ids.forEach { id -> db.delete(TABLE_ENTRIES, "id = ?", arrayOf(id)) }
      db.setTransactionSuccessful()
    } finally {
      db.endTransaction()
    }
    refresh()
  }

  override fun saveSettings(settings: AgentSettings) {
    dbHelper.writableDatabase.insertWithOnConflict(TABLE_SETTINGS, null, settings.toValues(), SQLiteDatabase.CONFLICT_REPLACE)
    refresh()
  }

  override fun getExistingTransactionIds(ids: List<String>): Set<String> {
    if (ids.isEmpty()) return emptySet()
    val result = mutableSetOf<String>()
    val db = dbHelper.readableDatabase
    val chunkSize = 50
    ids.chunked(chunkSize).forEach { chunk ->
      val placeholders = chunk.joinToString(",") { "?" }
      db.rawQuery(
        "SELECT transaction_id FROM $TABLE_ENTRIES WHERE transaction_id IN ($placeholders) AND transaction_id != ''",
        chunk.toTypedArray()
      ).use { cursor ->
        while (cursor.moveToNext()) {
          result.add(cursor.getString(0))
        }
      }
    }
    return result
  }

  override fun exportBackup(options: BackupOptions): ByteArray {
    require(options.includeEntries || options.includeSettings) { "请至少选择一种导出内容" }
    val payload = buildBackupPayload(options).toByteArray(Charsets.UTF_8)
    val encryptedPayload = encrypt(payload)
    return ByteArrayOutputStream().use { output ->
      ZipOutputStream(output).use { zip ->
        zip.putNextEntry(ZipEntry("manifest.json"))
        zip.write(
          JSONObject()
            .put("format", "AgenticLedger encrypted backup")
            .put("schema", BACKUP_SCHEMA)
            .put("encryption", "AES-256-GCM")
            .put("payload", "payload.enc")
            .toString(2)
            .toByteArray(Charsets.UTF_8)
        )
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("payload.enc"))
        zip.write(encryptedPayload)
        zip.closeEntry()
      }
      output.toByteArray()
    }
  }

  private fun buildBackupPayload(options: BackupOptions): String {
    val snapshot = loadSnapshot()
    val root =
      JSONObject()
        .put("schema", BACKUP_SCHEMA)
        .put("exportedAtMillis", Instant.now().toEpochMilli())
        .put(
          "includes",
          JSONObject()
            .put("entries", options.includeEntries)
            .put("settings", options.includeSettings)
        )

    if (options.includeSettings) root.put("settings", snapshot.settings.toJson())
    if (options.includeEntries) {
      val entries =
        snapshot.entries
          .filterBy(options.entryFilter)
          .sortedByDescending { it.occurredAtMillis }
      root.put("entryFilter", JSONObject().put("startDate", options.entryFilter.startDate).put("endDate", options.entryFilter.endDate))
      root.put("entries", JSONArray().also { array -> entries.forEach { array.put(it.toJson()) } })
    }
    return root.toString(2)
  }

  override fun importBackup(zipBytes: ByteArray) {
    val encryptedPayload =
      ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
        var entry = zip.nextEntry
        var payload: ByteArray? = null
        while (entry != null) {
          if (!entry.isDirectory && entry.name == "payload.enc") {
            payload = zip.readBytes()
          }
          zip.closeEntry()
          entry = zip.nextEntry
        }
        payload
      }
    requireNotNull(encryptedPayload) { "导入文件不是有效的 AgenticLedger 加密备份 ZIP" }
    importBackupPayload(decrypt(encryptedPayload).toString(Charsets.UTF_8))
  }

  private fun importBackupPayload(rawJson: String) {
    val root = JSONObject(rawJson)
    val includes = root.optJSONObject("includes") ?: JSONObject()
    val hasSettings = includes.optBoolean("settings", root.has("settings"))
    val hasEntries = includes.optBoolean("entries", root.has("entries"))
    require(hasSettings || hasEntries) { "导入文件没有包含可导入的数据" }

    val db = dbHelper.writableDatabase
    db.beginTransaction()
    try {
      if (hasSettings) {
        val settings = root.getJSONObject("settings").toAgentSettings().copy(isImportedFromBackup = true)
        db.delete(TABLE_SETTINGS, null, null)
        db.insertWithOnConflict(TABLE_SETTINGS, null, settings.toValues(), SQLiteDatabase.CONFLICT_REPLACE)
      }
      if (hasEntries) {
        val entries = root.optJSONArray("entries") ?: JSONArray()
        db.delete(TABLE_ENTRIES, null, null)
        for (index in 0 until entries.length()) {
          val entry = entries.getJSONObject(index).toLedgerEntry()
          db.insertWithOnConflict(TABLE_ENTRIES, null, entry.toValues(), SQLiteDatabase.CONFLICT_REPLACE)
        }
      }
      db.setTransactionSuccessful()
    } finally {
      db.endTransaction()
    }
    refresh()
  }

  private fun refresh() {
    _snapshot.value = loadSnapshot()
  }

  private fun loadSnapshot(): LedgerSnapshot =
    LedgerSnapshot(entries = loadEntries(), settings = loadSettings())

  private fun loadSettings(): AgentSettings {
    dbHelper.readableDatabase
      .query(TABLE_SETTINGS, null, "id = 1", null, null, null, null)
      .use { cursor ->
        if (cursor.moveToFirst()) {
          return AgentSettings(
            appTitle = cursor.getStringValue("app_title"),
            baseUrl = cursor.getStringValue("base_url"),
            apiKey = cursor.getStringValue("api_key"),
            modelName = cursor.getStringValue("model_name"),
            themeMode = AppThemeMode.fromStorageValue(cursor.getStringValue("theme_mode")),
            isImportedFromBackup = cursor.getIntValue("imported_from_backup") == 1,
            drawerExpanded = cursor.getIntValue("drawer_expanded") == 1,
          )
        }
      }
    return AgentSettings()
  }

  private fun loadEntries(): List<LedgerEntry> {
    val entries = mutableListOf<LedgerEntry>()
    dbHelper.readableDatabase
      .query(TABLE_ENTRIES, null, null, null, null, null, "occurred_at_millis DESC")
      .use { cursor ->
        while (cursor.moveToNext()) {
          entries +=
            LedgerEntry(
              id = cursor.getStringValue("id"),
              type = LedgerEntryType.fromStorageValue(cursor.getStringValue("entry_type")),
              amountCents = cursor.getLongValue("amount_cents"),
              channel = cursor.getStringValue("channel").ifBlank { "其他" },
              category = cursor.getStringValue("category").ifBlank { "其他" },
              description = cursor.getStringValue("description"),
              occurredAtMillis = cursor.getLongValue("occurred_at_millis"),
              rawText = cursor.getStringValue("raw_text"),
              createdAtMillis = cursor.getLongValue("created_at_millis"),
              transactionId = cursor.getStringValueOrNull("transaction_id"),
            )
        }
      }
    return entries
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

  private fun LedgerEntry.toValues(): ContentValues =
    ContentValues().apply {
      put("id", id)
      put("entry_type", type.storageValue)
      put("amount_cents", amountCents)
      put("channel", channel)
      put("category", category)
      put("description", description)
      put("occurred_at_millis", occurredAtMillis)
      put("raw_text", rawText)
      put("created_at_millis", createdAtMillis)
      put("transaction_id", transactionId)
    }

  private fun AgentSettings.toValues(): ContentValues =
    ContentValues().apply {
      put("id", 1)
      put("app_title", appTitle.trim())
      put("base_url", baseUrl.trim())
      put("api_key", apiKey.trim())
      put("model_name", modelName.trim())
      put("theme_mode", themeMode.storageValue)
      put("imported_from_backup", if (isImportedFromBackup) 1 else 0)
      put("drawer_expanded", if (drawerExpanded) 1 else 0)
    }
}

private fun encrypt(plainBytes: ByteArray): ByteArray {
  val iv = ByteArray(GCM_IV_SIZE_BYTES)
  SecureRandom().nextBytes(iv)
  val cipher = Cipher.getInstance(AES_TRANSFORMATION)
  cipher.init(Cipher.ENCRYPT_MODE, backupKey(), GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))
  return iv + cipher.doFinal(plainBytes)
}

private fun decrypt(encryptedBytes: ByteArray): ByteArray {
  require(encryptedBytes.size > GCM_IV_SIZE_BYTES) { "加密备份内容无效" }
  val iv = encryptedBytes.copyOfRange(0, GCM_IV_SIZE_BYTES)
  val ciphertext = encryptedBytes.copyOfRange(GCM_IV_SIZE_BYTES, encryptedBytes.size)
  val cipher = Cipher.getInstance(AES_TRANSFORMATION)
  cipher.init(Cipher.DECRYPT_MODE, backupKey(), GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))
  return cipher.doFinal(ciphertext)
}

private fun backupKey(): SecretKeySpec {
  val digest = MessageDigest.getInstance("SHA-256").digest(BACKUP_KEY_MATERIAL.toByteArray(Charsets.UTF_8))
  return SecretKeySpec(digest, "AES")
}

private fun LedgerEntry.toJson(): JSONObject =
  JSONObject()
    .put("id", id)
    .put("type", type.storageValue)
    .put("amountCents", amountCents)
    .put("channel", channel)
    .put("category", category)
    .put("description", description)
    .put("occurredAtMillis", occurredAtMillis)
    .put("rawText", rawText)
    .put("createdAtMillis", createdAtMillis)
    .put("transactionId", transactionId)

private fun JSONObject.toLedgerEntry(): LedgerEntry =
  LedgerEntry(
    id = getString("id"),
    type = LedgerEntryType.fromStorageValue(getString("type")),
    amountCents = getLong("amountCents"),
    channel = optString("channel", "其他"),
    category = optString("category", "其他"),
    description = optString("description", ""),
    occurredAtMillis = optLong("occurredAtMillis", Instant.now().toEpochMilli()),
    rawText = optString("rawText", ""),
    createdAtMillis = optLong("createdAtMillis", Instant.now().toEpochMilli()),
    transactionId = optString("transactionId", ""),
  )

private fun AgentSettings.toJson(): JSONObject =
  JSONObject()
    .put("appTitle", appTitle)
    .put("baseUrl", baseUrl)
    .put("apiKey", apiKey)
    .put("modelName", modelName)
    .put("themeMode", themeMode.storageValue)
    .put("isImportedFromBackup", isImportedFromBackup)
    .put("drawerExpanded", drawerExpanded)

private fun JSONObject.toAgentSettings(): AgentSettings =
  AgentSettings(
    appTitle = optString("appTitle", "AgenticLedger").ifBlank { "AgenticLedger" },
    baseUrl = optString("baseUrl", ""),
    apiKey = optString("apiKey", ""),
    modelName = optString("modelName", ""),
    themeMode = AppThemeMode.fromStorageValue(optString("themeMode", AppThemeMode.System.storageValue)),
    isImportedFromBackup = optBoolean("isImportedFromBackup", false),
    drawerExpanded = optBoolean("drawerExpanded", true),
  )

private class LedgerDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE $TABLE_SETTINGS (
        id INTEGER PRIMARY KEY CHECK (id = 1),
        app_title TEXT NOT NULL,
        base_url TEXT NOT NULL,
        api_key TEXT NOT NULL,
        model_name TEXT NOT NULL,
        theme_mode TEXT NOT NULL DEFAULT 'SYSTEM',
        imported_from_backup INTEGER NOT NULL DEFAULT 0,
        drawer_expanded INTEGER NOT NULL DEFAULT 1
      )
      """.trimIndent()
    )
    db.execSQL(
      """
      CREATE TABLE $TABLE_ENTRIES (
        id TEXT PRIMARY KEY,
        entry_type TEXT NOT NULL DEFAULT 'EXPENSE',
        amount_cents INTEGER NOT NULL,
        channel TEXT NOT NULL,
        category TEXT NOT NULL,
        description TEXT NOT NULL,
        occurred_at_millis INTEGER NOT NULL,
        raw_text TEXT NOT NULL,
        created_at_millis INTEGER NOT NULL,
        transaction_id TEXT NOT NULL DEFAULT ''
      )
      """.trimIndent()
    )
    db.execSQL("CREATE INDEX idx_entries_occurred_at ON $TABLE_ENTRIES(occurred_at_millis)")
    db.execSQL("CREATE INDEX idx_entries_transaction_id ON $TABLE_ENTRIES(transaction_id)")
    db.insertWithOnConflict(TABLE_SETTINGS, null, AgentSettings().toValuesForCreate(), SQLiteDatabase.CONFLICT_REPLACE)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    // The app is pre-release; old prototype storage formats are intentionally not migrated.
    if (oldVersion < 4) {
      db.execSQL("ALTER TABLE $TABLE_SETTINGS ADD COLUMN drawer_expanded INTEGER NOT NULL DEFAULT 1")
    }
    if (oldVersion < 5) {
      db.execSQL("ALTER TABLE $TABLE_ENTRIES ADD COLUMN transaction_id TEXT NOT NULL DEFAULT ''")
      db.execSQL("CREATE INDEX idx_entries_transaction_id ON $TABLE_ENTRIES(transaction_id)")
    }
  }
}

private fun android.database.Cursor.getStringValue(column: String): String =
  getString(getColumnIndexOrThrow(column)).orEmpty()

private fun android.database.Cursor.getStringValueOrNull(column: String): String {
  val index = getColumnIndex(column)
  return if (index >= 0 && !isNull(index)) getString(index).orEmpty() else ""
}

private fun android.database.Cursor.getLongValue(column: String): Long =
  getLong(getColumnIndexOrThrow(column))

private fun android.database.Cursor.getIntValue(column: String): Int =
  getInt(getColumnIndexOrThrow(column))

private fun AgentSettings.toValuesForCreate(): ContentValues =
  ContentValues().apply {
    put("id", 1)
    put("app_title", appTitle)
    put("base_url", baseUrl)
    put("api_key", apiKey)
    put("model_name", modelName)
    put("theme_mode", themeMode.storageValue)
    put("imported_from_backup", if (isImportedFromBackup) 1 else 0)
    put("drawer_expanded", if (drawerExpanded) 1 else 0)
  }

private const val DB_NAME = "agenticledger.db"
private const val DB_VERSION = 5
private const val BACKUP_SCHEMA = 2
private const val TABLE_SETTINGS = "settings"
private const val TABLE_ENTRIES = "entries"
private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_IV_SIZE_BYTES = 12
private const val GCM_TAG_SIZE_BITS = 128
private const val BACKUP_KEY_MATERIAL = "io.github.cming0420.agenticledger.agenticledger.backup.v1"
