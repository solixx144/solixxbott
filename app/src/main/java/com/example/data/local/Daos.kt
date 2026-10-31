package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE status = :status ORDER BY id DESC")
    fun getAnnouncementsByStatusFlow(status: String): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE id = :id LIMIT 1")
    suspend fun getAnnouncementById(id: Long): AnnouncementEntity?

    @Query("SELECT COUNT(*) FROM announcements")
    fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM announcements WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM announcements WHERE status = 'APPROVED'")
    fun getPublishedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM announcements WHERE status = 'REJECTED'")
    fun getRejectedCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(announcement: AnnouncementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(announcements: List<AnnouncementEntity>)

    @Update
    suspend fun update(announcement: AnnouncementEntity)

    @Query("""
        UPDATE announcements 
        SET status = 'APPROVED', 
            publishedAt = :publishedAt, 
            publishedBy = :publishedBy, 
            publishedMessageIdsJson = :msgIdsJson, 
            updatedAt = :publishedAt 
        WHERE id = :id AND status = 'PENDING'
    """)
    suspend fun approveAtomic(id: Long, publishedAt: String, publishedBy: String, msgIdsJson: String): Int

    @Query("""
        UPDATE announcements 
        SET status = 'REJECTED', 
            rejectionReason = :reason, 
            adminNotes = :notes, 
            updatedAt = :now 
        WHERE id = :id AND status = 'PENDING'
    """)
    suspend fun rejectAtomic(id: Long, reason: String, notes: String?, now: String): Int

    @Query("""
        UPDATE announcements 
        SET textContent = :newText, 
            adminNotes = :notes, 
            updatedAt = :now 
        WHERE id = :id AND status = 'PENDING'
    """)
    suspend fun editContent(id: Long, newText: String, notes: String?, now: String): Int

    @Query("SELECT * FROM announcements WHERE senderTelegramId = :telegramId ORDER BY id DESC")
    suspend fun getBySender(telegramId: Long): List<AnnouncementEntity>

    @Query("DELETE FROM announcements")
    suspend fun clearAll()
}

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY addedAt DESC")
    fun getAllFriendsFlow(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE telegramId = :telegramId LIMIT 1")
    suspend fun getFriendByTelegramId(telegramId: Long): FriendEntity?

    @Query("SELECT COUNT(*) FROM friends WHERE isActive = 1")
    fun getActiveFriendsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(friend: FriendEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<FriendEntity>)

    @Query("DELETE FROM friends WHERE telegramId = :telegramId")
    suspend fun deleteByTelegramId(telegramId: Long): Int

    @Query("UPDATE friends SET isActive = :isActive WHERE telegramId = :telegramId")
    suspend fun toggleActive(telegramId: Long, isActive: Boolean): Int

    @Query("DELETE FROM friends")
    suspend fun clearAll()
}

@Dao
interface LogDao {
    @Query("SELECT * FROM system_logs ORDER BY id DESC LIMIT :limit")
    fun getRecentLogsFlow(limit: Int = 50): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: LogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<LogEntity>)

    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()
}
