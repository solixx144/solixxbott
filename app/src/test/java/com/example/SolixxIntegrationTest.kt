package com.example

import com.example.data.model.Announcement
import com.example.data.model.Friend
import com.example.data.model.ServerConfig
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.HistoryFilter
import com.example.ui.viewmodel.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SolixxIntegrationTest {

    @Test
    fun testServerConfigDefaultsAndPersistenceModel() {
        val defaultConfig = ServerConfig()
        assertEquals("http://10.0.2.2:8000", defaultConfig.serverUrl)
        assertEquals("@SolixxTR", defaultConfig.targetChannel)
        assertTrue(defaultConfig.notificationsEnabled)
        assertEquals(30, defaultConfig.autoSyncInterval)

        val updatedConfig = defaultConfig.copy(
            serverUrl = "http://192.168.1.50:8000",
            apiKey = "admin_super_secret_key",
            adminTelegramId = 987654321L,
            targetChannel = "@SolixxChannel",
            notificationsEnabled = false,
            autoSyncInterval = 60
        )
        assertEquals("http://192.168.1.50:8000", updatedConfig.serverUrl)
        assertEquals("admin_super_secret_key", updatedConfig.apiKey)
        assertEquals(987654321L, updatedConfig.adminTelegramId)
        assertEquals("@SolixxChannel", updatedConfig.targetChannel)
        assertFalse(updatedConfig.notificationsEnabled)
        assertEquals(60, updatedConfig.autoSyncInterval)
    }

    @Test
    fun testFilteredFriendsDeduplicationAndSafeHandling() {
        val f1 = Friend(id = 1, telegramId = 12345, displayName = "Ali", addedAt = "2026-03-01 10:00:00")
        val f2 = Friend(id = 2, telegramId = 12345, displayName = "Ali Duplicate", addedAt = "2026-03-01 10:05:00")
        val f3 = Friend(id = 3, telegramId = 67890, displayName = "Mehmet", addedAt = "2026-03-01 11:00:00")

        val state = UiState(friends = listOf(f1, f2, f3))

        // Ensure duplicates are removed by telegramId
        assertEquals(2, state.filteredFriends.size)
        assertEquals(12345L, state.filteredFriends[0].telegramId)
        assertEquals(67890L, state.filteredFriends[1].telegramId)

        // Search test
        val searchedState = state.copy(friendSearchQuery = "mehmet")
        assertEquals(1, searchedState.filteredFriends.size)
        assertEquals("Mehmet", searchedState.filteredFriends[0].displayName)
    }

    @Test
    fun testFilteredAnnouncementsByStatusAndSearch() {
        val a1 = Announcement(
            id = 1,
            senderTelegramId = 100,
            senderUsername = "user1",
            senderDisplayName = "Duyuru Gönderen 1",
            contentType = "TEXT",
            textContent = "Duyuru 1",
            status = "PENDING",
            createdAt = "2026-03-01 10:00:00",
            updatedAt = "2026-03-01 10:00:00"
        )
        val a2 = Announcement(
            id = 2,
            senderTelegramId = 200,
            senderUsername = "user2",
            senderDisplayName = "Duyuru Gönderen 2",
            contentType = "TEXT",
            textContent = "Duyuru 2",
            status = "APPROVED",
            createdAt = "2026-03-01 11:00:00",
            updatedAt = "2026-03-01 11:00:00"
        )
        val a3 = Announcement(
            id = 3,
            senderTelegramId = 300,
            senderUsername = "user3",
            senderDisplayName = "Duyuru Gönderen 3",
            contentType = "TEXT",
            textContent = "Önemli Haber",
            status = "REJECTED",
            createdAt = "2026-03-01 12:00:00",
            updatedAt = "2026-03-01 12:00:00"
        )

        val state = UiState(allAnnouncements = listOf(a1, a2, a3))

        // Filter by Published (APPROVED)
        val publishedState = state.copy(historyFilter = HistoryFilter.PUBLISHED)
        assertEquals(1, publishedState.filteredHistory.size)
        assertEquals(2L, publishedState.filteredHistory[0].id)

        // Search text
        val searched = state.copy(historyFilter = HistoryFilter.ALL, searchQuery = "Haber")
        assertEquals(1, searched.filteredHistory.size)
        assertEquals(3L, searched.filteredHistory[0].id)
    }

    @Test
    fun testSystemLogFilteringAndErrorCategories() {
        val l1 = com.example.data.model.SystemLog(
            id = 1,
            timestamp = "2026-03-01 12:00:00",
            level = "ERROR",
            category = "NETWORK",
            message = "Ağ Bağlantı Hatası: DNS Çözümlenemedi",
            details = "Endpoint: http://10.0.2.2:8000/api/status"
        )
        val l2 = com.example.data.model.SystemLog(
            id = 2,
            timestamp = "2026-03-01 12:01:00",
            level = "ERROR",
            category = "API",
            message = "API Hatası: 401 Unauthorized",
            details = "Endpoint: /api/announcements/approve"
        )
        val l3 = com.example.data.model.SystemLog(
            id = 3,
            timestamp = "2026-03-01 12:02:00",
            level = "ERROR",
            category = "JSON_PARSE",
            message = "JSON Çözümleme Hatası: Expected boolean",
            details = "Raw: {is_active: invalid}"
        )
        val l4 = com.example.data.model.SystemLog(
            id = 4,
            timestamp = "2026-03-01 12:03:00",
            level = "INFO",
            category = "SYSTEM",
            message = "Sistem başarıyla başlatıldı",
            details = null
        )

        val logs = listOf(l1, l2, l3, l4)
        val errorLogs = logs.filter { it.level == "ERROR" }
        assertEquals(3, errorLogs.size)

        val networkLogs = logs.filter { it.category == "NETWORK" }
        assertEquals(1, networkLogs.size)
        assertEquals("Ağ Bağlantı Hatası: DNS Çözümlenemedi", networkLogs[0].message)

        val apiLogs = logs.filter { it.category == "API" }
        assertEquals(1, apiLogs.size)
        assertEquals("API Hatası: 401 Unauthorized", apiLogs[0].message)

        val jsonLogs = logs.filter { it.category == "JSON_PARSE" }
        assertEquals(1, jsonLogs.size)
        assertEquals("JSON Çözümleme Hatası: Expected boolean", jsonLogs[0].message)
    }
}
