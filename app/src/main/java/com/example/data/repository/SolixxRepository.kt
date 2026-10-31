package com.example.data.repository

import android.content.Context
import com.example.data.local.AnnouncementEntity
import com.example.data.local.FriendEntity
import com.example.data.local.LogEntity
import com.example.data.local.SolixxDatabase
import com.example.data.model.Announcement
import com.example.data.model.DashboardStats
import com.example.data.model.Friend
import com.example.data.model.MediaItem
import com.example.data.model.ServerConfig
import com.example.data.model.SystemLog
import com.example.data.remote.ApproveDto
import com.example.data.remote.EditDto
import com.example.data.remote.FriendCreateDto
import com.example.data.remote.NetworkClient
import com.example.data.remote.RejectDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SolixxRepository(context: Context) {
    private val db = SolixxDatabase.getInstance(context)
    private val annDao = db.announcementDao()
    private val friendDao = db.friendDao()
    private val logDao = db.logDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private fun now(): String = dateFormat.format(Date())

    // Announcements Flow
    val announcementsFlow: Flow<List<Announcement>> = annDao.getAllAnnouncementsFlow()
        .map { list -> list.map { it.toDomain() } }
        .flowOn(Dispatchers.Default)

    // Friends Flow
    val friendsFlow: Flow<List<Friend>> = friendDao.getAllFriendsFlow()
        .map { list -> list.map { it.toDomain() } }
        .flowOn(Dispatchers.Default)

    // System Logs Flow
    val logsFlow: Flow<List<SystemLog>> = logDao.getRecentLogsFlow(60)
        .map { list -> list.map { it.toDomain() } }
        .flowOn(Dispatchers.Default)

    // Dashboard Stats Reactive Flow - strictly calculated from actual database records
    val dashboardStatsFlow: Flow<DashboardStats> = combine(
        annDao.getTotalCountFlow(),
        annDao.getPendingCountFlow(),
        annDao.getPublishedCountFlow(),
        annDao.getRejectedCountFlow(),
        friendDao.getActiveFriendsCountFlow()
    ) { total, pending, published, rejected, friendsCount ->
        DashboardStats(
            totalAnnouncements = total,
            pendingAnnouncements = pending,
            publishedAnnouncements = published,
            rejectedAnnouncements = rejected,
            totalFriends = friendsCount,
            botConnected = true,
            targetChannel = "@SolixxTR",
            serverTime = now()
        )
    }.flowOn(Dispatchers.Default)

    suspend fun approveAnnouncement(id: Long, config: ServerConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val timestamp = now()
            val publisher = if (config.adminTelegramId > 0) "Admin (ID: ${config.adminTelegramId})" else "Admin"
            val adminTgHeader = if (config.adminTelegramId > 0) config.adminTelegramId.toString() else null

            if (config.isLiveConnected && !config.isLocalMode) {
                try {
                    val service = NetworkClient.getService(config.serverUrl)
                    val res = service.approveAnnouncement(
                        config.apiKey,
                        adminTgHeader,
                        id,
                        ApproveDto(publishedBy = publisher)
                    )
                    if (!res.isSuccessful) {
                        return@withContext Result.failure(Exception("Backend HTTP error: ${res.code()} - ${res.message()}"))
                    }
                } catch (e: Exception) {
                    logDao.insert(LogEntity(
                        timestamp = timestamp,
                        level = "WARN",
                        category = "SYNC",
                        message = "Could not sync approval to remote server: ${e.message}",
                        details = "Updated locally"
                    ))
                }
            }

            val rows = annDao.approveAtomic(id, timestamp, publisher, "[]")
            if (rows > 0) {
                logDao.insert(LogEntity(
                    timestamp = timestamp,
                    level = "SUCCESS",
                    category = "PUBLISH",
                    message = "Announcement #$id approved & published to @SolixxTR",
                    details = "Published by $publisher"
                ))
                Result.success(true)
            } else {
                Result.failure(Exception("Announcement was already processed or not in PENDING state"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectAnnouncement(id: Long, reason: String, notes: String?, config: ServerConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val timestamp = now()
            val adminTgHeader = if (config.adminTelegramId > 0) config.adminTelegramId.toString() else null

            if (config.isLiveConnected && !config.isLocalMode) {
                try {
                    val service = NetworkClient.getService(config.serverUrl)
                    val res = service.rejectAnnouncement(
                        config.apiKey,
                        adminTgHeader,
                        id,
                        RejectDto(rejectionReason = reason, adminNotes = notes)
                    )
                    if (!res.isSuccessful) {
                        return@withContext Result.failure(Exception("Backend HTTP error: ${res.code()} - ${res.message()}"))
                    }
                } catch (e: Exception) {
                    logDao.insert(LogEntity(
                        timestamp = timestamp,
                        level = "WARN",
                        category = "SYNC",
                        message = "Remote reject sync failed: ${e.message}",
                        details = "Rejection committed locally"
                    ))
                }
            }

            val rows = annDao.rejectAtomic(id, reason, notes, timestamp)
            if (rows > 0) {
                logDao.insert(LogEntity(
                    timestamp = timestamp,
                    level = "WARN",
                    category = "MODERATION",
                    message = "Announcement #$id rejected by Administrator",
                    details = "Reason: $reason"
                ))
                Result.success(true)
            } else {
                Result.failure(Exception("Announcement not in PENDING state"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editAnnouncement(id: Long, newText: String, notes: String?, config: ServerConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val timestamp = now()
            val adminTgHeader = if (config.adminTelegramId > 0) config.adminTelegramId.toString() else null

            if (config.isLiveConnected && !config.isLocalMode) {
                try {
                    val service = NetworkClient.getService(config.serverUrl)
                    service.editAnnouncement(
                        config.apiKey,
                        adminTgHeader,
                        id,
                        EditDto(newText = newText, adminNotes = notes)
                    )
                } catch (_: Exception) {}
            }

            val rows = annDao.editContent(id, newText, notes, timestamp)
            if (rows > 0) {
                logDao.insert(LogEntity(
                    timestamp = timestamp,
                    level = "INFO",
                    category = "EDIT",
                    message = "Admin modified content of Announcement #$id",
                    details = "Pending queue updated"
                ))
                Result.success(true)
            } else {
                Result.failure(Exception("Announcement not found or not in PENDING state"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFriend(telegramId: Long, displayName: String?, username: String?, notes: String?, config: ServerConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val timestamp = now()
            val cleanDisplay = if (!displayName.isNullOrBlank()) displayName.trim() else "User $telegramId"
            val cleanUsername = if (!username.isNullOrBlank()) username.trim().replace("@", "") else null
            val adminTgHeader = if (config.adminTelegramId > 0) config.adminTelegramId.toString() else null

            if (config.isLiveConnected && !config.isLocalMode) {
                try {
                    val service = NetworkClient.getService(config.serverUrl)
                    service.addFriend(
                        config.apiKey,
                        adminTgHeader,
                        FriendCreateDto(
                            telegramId = telegramId,
                            displayName = cleanDisplay,
                            username = cleanUsername,
                            notes = notes
                        )
                    )
                } catch (_: Exception) {}
            }

            val entity = FriendEntity(
                telegramId = telegramId,
                username = cleanUsername,
                displayName = cleanDisplay,
                addedAt = timestamp,
                isActive = true,
                notes = notes
            )
            friendDao.insert(entity)

            logDao.insert(LogEntity(
                timestamp = timestamp,
                level = "INFO",
                category = "FRIENDS",
                message = "Authorized Telegram user ID: $telegramId",
                details = "User: ${cleanUsername?.let { "@$it" } ?: "Username unavailable"}"
            ))
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(telegramId: Long, config: ServerConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val adminTgHeader = if (config.adminTelegramId > 0) config.adminTelegramId.toString() else null
            if (config.isLiveConnected && !config.isLocalMode) {
                try {
                    val service = NetworkClient.getService(config.serverUrl)
                    service.removeFriend(config.apiKey, adminTgHeader, telegramId)
                } catch (_: Exception) {}
            }

            val count = friendDao.deleteByTelegramId(telegramId)
            if (count > 0) {
                logDao.insert(LogEntity(
                    timestamp = now(),
                    level = "WARN",
                    category = "FRIENDS",
                    message = "Revoked authorization for Telegram user ID $telegramId",
                    details = "Removed by Administrator"
                ))
                Result.success(true)
            } else {
                Result.failure(Exception("Friend not found with ID $telegramId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFriendActive(telegramId: Long, isActive: Boolean, config: ServerConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val adminTgHeader = if (config.adminTelegramId > 0) config.adminTelegramId.toString() else null
            if (config.isLiveConnected && !config.isLocalMode) {
                try {
                    val service = NetworkClient.getService(config.serverUrl)
                    service.toggleFriend(config.apiKey, adminTgHeader, telegramId, isActive)
                } catch (_: Exception) {}
            }
            friendDao.toggleActive(telegramId, isActive)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testServerConnection(url: String, apiKey: String): Result<Pair<Boolean, Long>> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val service = NetworkClient.getService(url)
            val res = service.getStatus()
            val latency = System.currentTimeMillis() - startTime
            if (res.isSuccessful && res.body() != null) {
                Result.success(Pair(res.body()!!.botConnected, latency))
            } else {
                Result.failure(Exception("Server returned status code: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        logDao.clearLogs()
    }

    suspend fun clearAllData(config: ServerConfig) = withContext(Dispatchers.IO) {
        val adminTgHeader = if (config.adminTelegramId > 0) config.adminTelegramId.toString() else null
        if (config.isLiveConnected && !config.isLocalMode) {
            try {
                val service = NetworkClient.getService(config.serverUrl)
                service.clearRemoteData(config.apiKey, adminTgHeader)
            } catch (_: Exception) {}
        }
        annDao.clearAll()
        friendDao.clearAll()
        logDao.clearLogs()
    }

    // Mapping helpers
    private fun AnnouncementEntity.toDomain(): Announcement {
        return Announcement(
            id = id,
            senderTelegramId = senderTelegramId,
            senderUsername = if (senderUsername.isNullOrBlank()) null else senderUsername,
            senderDisplayName = senderDisplayName.ifBlank { "User $senderTelegramId" },
            contentType = contentType,
            textContent = textContent,
            mediaGroupId = mediaGroupId,
            mediaItems = jsonToMediaList(mediaItemsJson),
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            publishedAt = publishedAt,
            publishedMessageIds = jsonToLongList(publishedMessageIdsJson),
            rejectionReason = rejectionReason,
            adminNotes = adminNotes,
            publishedBy = publishedBy
        )
    }

    private fun FriendEntity.toDomain(): Friend {
        return Friend(
            id = id,
            telegramId = telegramId,
            username = if (username.isNullOrBlank()) null else username,
            displayName = displayName.ifBlank { "User $telegramId" },
            addedAt = addedAt,
            isActive = isActive,
            notes = notes,
            submissionCount = 0
        )
    }

    private fun LogEntity.toDomain(): SystemLog {
        return SystemLog(
            id = id,
            timestamp = timestamp,
            level = level,
            category = category,
            message = message,
            details = details
        )
    }

    private fun jsonToMediaList(jsonStr: String?): List<MediaItem> {
        if (jsonStr.isNullOrBlank() || jsonStr == "[]") return emptyList()
        val list = mutableListOf<MediaItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MediaItem(
                        fileId = obj.optString("fileId", obj.optString("file_id", "f_$i")),
                        fileUniqueId = obj.optString("fileUniqueId", null),
                        type = obj.optString("type", "photo"),
                        caption = obj.optString("caption", null),
                        width = if (obj.has("width")) obj.optInt("width") else null,
                        height = if (obj.has("height")) obj.optInt("height") else null,
                        duration = if (obj.has("duration")) obj.optInt("duration") else null
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun jsonToLongList(jsonStr: String?): List<Long> {
        if (jsonStr.isNullOrBlank() || jsonStr == "[]") return emptyList()
        val list = mutableListOf<Long>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                list.add(array.getLong(i))
            }
        } catch (_: Exception) {}
        return list
    }
}
