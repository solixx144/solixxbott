package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderTelegramId: Long,
    val senderUsername: String?,
    val senderDisplayName: String,
    val contentType: String,
    val textContent: String?,
    val mediaGroupId: String?,
    val mediaItemsJson: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val publishedAt: String?,
    val publishedMessageIdsJson: String?,
    val rejectionReason: String?,
    val adminNotes: String?,
    val publishedBy: String?
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val telegramId: Long,
    val username: String?,
    val displayName: String,
    val addedAt: String,
    val isActive: Boolean,
    val notes: String?
)

@Entity(tableName = "system_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: String,
    val level: String,
    val category: String,
    val message: String,
    val details: String?
)
