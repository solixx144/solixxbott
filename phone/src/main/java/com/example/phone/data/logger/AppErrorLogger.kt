package com.example.phone.data.logger

import android.content.Context
import com.example.phone.data.local.LogEntity
import com.example.phone.data.local.SolixxDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppErrorLogger {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var database: SolixxDatabase? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun initialize(context: Context) {
        if (database == null) {
            database = SolixxDatabase.getInstance(context.applicationContext)
        }
    }

    private fun getNow(): String = dateFormat.format(Date())

    private fun saveLog(level: String, category: String, message: String, details: String?) {
        val db = database ?: return
        scope.launch {
            try {
                db.logDao().insert(
                    LogEntity(
                        timestamp = getNow(),
                        level = level,
                        category = category,
                        message = message,
                        details = details
                    )
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Log network connectivity failure (UnknownHost, SocketTimeout, ConnectException, etc.)
     */
    fun logNetworkError(endpoint: String, throwable: Throwable, customMessage: String? = null) {
        val rootCause = when (throwable) {
            is UnknownHostException -> "DNS Çözümlenemedi / İnternet Bağlantısı Yok (${throwable.message})"
            is SocketTimeoutException -> "Bağlantı Zaman Aşımına Uğradı (Timeout)"
            is ConnectException -> "Sunucuya Bağlanılamadı / Bağlantı Reddedildi (Connection Refused)"
            else -> throwable.localizedMessage ?: throwable.javaClass.simpleName
        }

        val message = customMessage ?: "Ağ Bağlantı Hatası: $rootCause"
        val details = buildString {
            append("Endpoint: ").append(endpoint).append("\n")
            append("Hata Türü: ").append(throwable.javaClass.name).append("\n")
            append("Açıklama: ").append(rootCause).append("\n")
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val trace = sw.toString().lines().take(6).joinToString("\n")
            append("Yığın İzi:\n").append(trace)
        }

        saveLog("ERROR", "NETWORK", message, details)
    }

    /**
     * Log HTTP REST API response error (4xx, 5xx)
     */
    fun logApiError(endpoint: String, statusCode: Int, responseBody: String?, reason: String? = null) {
        val statusDesc = when (statusCode) {
            400 -> "400 Bad Request (Hatalı İstek Parametreleri)"
            401 -> "401 Unauthorized (Geçersiz veya Eksik X-Admin-Key)"
            403 -> "403 Forbidden (Yönetici Telegram ID Eşleşmedi)"
            404 -> "404 Not Found (Endpoint veya Kaynak Bulunamadı)"
            422 -> "422 Unprocessable Entity (Veri Doğrulama Hatası)"
            500 -> "500 Internal Server Error (Sunucu İçi Hata)"
            502 -> "502 Bad Gateway (Ağ Geçidi Hatası)"
            503 -> "503 Service Unavailable (Sunucu Servis Dışı)"
            else -> "$statusCode HTTP Hatası"
        }

        val message = "API Hatası: $statusDesc [$endpoint]"
        val details = buildString {
            append("İstek Adresi: ").append(endpoint).append("\n")
            append("HTTP Durum Kodu: ").append(statusCode).append(" (").append(statusDesc).append(")\n")
            if (!reason.isNullOrBlank()) {
                append("Gerekçe: ").append(reason).append("\n")
            }
            if (!responseBody.isNullOrBlank()) {
                append("Sunucu Yanıtı: ").append(responseBody.take(500))
            }
        }

        saveLog("ERROR", "API", message, details)
    }

    /**
     * Log JSON parsing / Deserialization error (Moshi, JsonDataException, etc.)
     */
    fun logJsonParseError(endpoint: String, rawJsonSnippet: String?, throwable: Throwable) {
        val message = "JSON Çözümleme Hatası: ${throwable.localizedMessage ?: throwable.javaClass.simpleName}"
        val details = buildString {
            append("Kaynak: ").append(endpoint).append("\n")
            append("Ayrıştırıcı Hatası: ").append(throwable.localizedMessage).append("\n")
            if (!rawJsonSnippet.isNullOrBlank()) {
                append("Ham JSON Kesiti: ").append(rawJsonSnippet.take(300)).append("\n")
            }
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val trace = sw.toString().lines().take(5).joinToString("\n")
            append("Yığın İzi:\n").append(trace)
        }

        saveLog("ERROR", "JSON_PARSE", message, details)
    }

    /**
     * Log general application error
     */
    fun logError(category: String, message: String, throwable: Throwable? = null) {
        val details = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString().lines().take(6).joinToString("\n")
        }
        saveLog("ERROR", category, message, details)
    }

    fun logWarning(category: String, message: String, details: String? = null) {
        saveLog("WARN", category, message, details)
    }

    fun logInfo(category: String, message: String, details: String? = null) {
        saveLog("INFO", category, message, details)
    }

    /**
     * Simulate test errors to allow administrator to test the diagnostic interface
     */
    fun simulateTestError(type: String) {
        when (type) {
            "NETWORK" -> {
                logNetworkError(
                    endpoint = "http://10.0.2.2:8000/api/status",
                    throwable = ConnectException("Failed to connect to /10.0.2.2:8000 (Connection refused)"),
                    customMessage = "Simüle Edilmiş Ağ Hatası: Sunucu kapalı veya ulaşılamıyor"
                )
            }
            "API" -> {
                logApiError(
                    endpoint = "/api/announcements/approve",
                    statusCode = 401,
                    responseBody = "{\"detail\":\"Unauthorized: Invalid Administrator API Key\"}",
                    reason = "API anahtarı doğrulaması başarısız oldu"
                )
            }
            "JSON_PARSE" -> {
                logJsonParseError(
                    endpoint = "/api/friends",
                    rawJsonSnippet = "{\"telegram_id\": \"not_a_number\", \"is_active\": \"invalid_bool\"}",
                    throwable = IllegalArgumentException("Expected a long but was STRING at path $.telegram_id")
                )
            }
            else -> {
                logError("SYSTEM", "Simüle edilmiş genel sistem denetim uyarısı", Exception("Diagnostic test exception"))
            }
        }
    }
}
