package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ServerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "solixx_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_API_KEY = stringPreferencesKey("api_key")
        val KEY_ADMIN_TELEGRAM_ID = longPreferencesKey("admin_telegram_id")
        val KEY_TARGET_CHANNEL = stringPreferencesKey("target_channel")
        val KEY_IS_LOCAL_MODE = booleanPreferencesKey("is_local_mode")

        const val DEFAULT_SERVER_URL = "http://10.0.2.2:8000"
        const val DEFAULT_TARGET_CHANNEL = "@SolixxTR"
        const val DEFAULT_LOCAL_MODE = true
    }

    val serverConfigFlow: Flow<ServerConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            ServerConfig(
                serverUrl = preferences[KEY_SERVER_URL] ?: DEFAULT_SERVER_URL,
                apiKey = preferences[KEY_API_KEY] ?: "",
                adminTelegramId = preferences[KEY_ADMIN_TELEGRAM_ID] ?: 0L,
                targetChannel = preferences[KEY_TARGET_CHANNEL] ?: DEFAULT_TARGET_CHANNEL,
                isLiveConnected = false,
                latencyMs = null,
                isLocalMode = preferences[KEY_IS_LOCAL_MODE] ?: DEFAULT_LOCAL_MODE
            )
        }

    suspend fun updateSettings(
        serverUrl: String,
        apiKey: String,
        adminTelegramId: Long,
        targetChannel: String,
        isLocalMode: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SERVER_URL] = serverUrl.trim()
            preferences[KEY_API_KEY] = apiKey.trim()
            preferences[KEY_ADMIN_TELEGRAM_ID] = adminTelegramId
            preferences[KEY_TARGET_CHANNEL] = if (targetChannel.trim().isNotEmpty()) targetChannel.trim() else DEFAULT_TARGET_CHANNEL
            preferences[KEY_IS_LOCAL_MODE] = isLocalMode
        }
    }

    suspend fun clearSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
