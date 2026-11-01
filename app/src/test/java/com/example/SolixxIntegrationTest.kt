package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SettingsDataStore
import com.example.data.model.Friend
import com.example.data.model.ServerConfig
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SolixxIntegrationTest {

    @Test
    fun testSettingsDataStorePersistence() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dataStore = SettingsDataStore(context)

        // Save custom settings
        dataStore.updateSettings(
            serverUrl = "http://192.168.1.100:8000",
            apiKey = "secret_solixx_key",
            adminTelegramId = 987654321L,
            targetChannel = "@SolixxTR",
            isLocalMode = false
        )

        // Read and verify
        val loaded = dataStore.serverConfigFlow.first()
        assertEquals("http://192.168.1.100:8000", loaded.serverUrl)
        assertEquals("secret_solixx_key", loaded.apiKey)
        assertEquals(987654321L, loaded.adminTelegramId)
        assertEquals("@SolixxTR", loaded.targetChannel)
        assertFalse(loaded.isLocalMode)
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
}
