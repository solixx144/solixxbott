package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Announcement
import com.example.data.model.DashboardStats
import com.example.data.model.Friend
import com.example.data.model.ServerConfig
import com.example.data.model.SystemLog
import com.example.data.repository.SolixxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    ANNOUNCEMENTS("Announcements"),
    FRIENDS("Friends"),
    HISTORY("History"),
    SETTINGS("Settings")
}

enum class HistoryFilter(val label: String) {
    ALL("All"),
    PUBLISHED("Published"),
    REJECTED("Rejected"),
    PENDING("Pending")
}

data class UiState(
    val currentTab: AppTab = AppTab.DASHBOARD,
    val stats: DashboardStats = DashboardStats(),
    val allAnnouncements: List<Announcement> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val logs: List<SystemLog> = emptyList(),
    val serverConfig: ServerConfig = ServerConfig(),
    val historyFilter: HistoryFilter = HistoryFilter.ALL,
    val searchQuery: String = "",
    val friendSearchQuery: String = "",
    val rejectingAnnouncement: Announcement? = null,
    val editingAnnouncement: Announcement? = null,
    val previewingAnnouncement: Announcement? = null,
    val showingAddFriendDialog: Boolean = false,
    val viewingFriendHistory: Friend? = null,
    val isTestingConnection: Boolean = false,
    val toastMessage: String? = null
) {
    val pendingAnnouncements: List<Announcement>
        get() = allAnnouncements.filter { it.status == "PENDING" }

    val publishedAnnouncements: List<Announcement>
        get() = allAnnouncements.filter { it.status == "APPROVED" }

    val rejectedAnnouncements: List<Announcement>
        get() = allAnnouncements.filter { it.status == "REJECTED" }

    val filteredFriends: List<Friend>
        get() {
            val list = friends.distinctBy { it.telegramId }
            return if (friendSearchQuery.isBlank()) list
            else list.filter {
                it.displayName.contains(friendSearchQuery, ignoreCase = true) ||
                (it.username?.contains(friendSearchQuery, ignoreCase = true) == true) ||
                it.telegramId.toString().contains(friendSearchQuery)
            }
        }

    val filteredHistory: List<Announcement>
        get() {
            val byStatus = when (historyFilter) {
                HistoryFilter.ALL -> allAnnouncements
                HistoryFilter.PUBLISHED -> publishedAnnouncements
                HistoryFilter.REJECTED -> rejectedAnnouncements
                HistoryFilter.PENDING -> pendingAnnouncements
            }
            return if (searchQuery.isBlank()) byStatus
            else byStatus.filter {
                (it.textContent?.contains(searchQuery, ignoreCase = true) == true) ||
                it.senderDisplayName.contains(searchQuery, ignoreCase = true) ||
                (it.senderUsername?.contains(searchQuery, ignoreCase = true) == true) ||
                it.senderTelegramId.toString().contains(searchQuery)
            }
        }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SolixxRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = combine(
        _uiState,
        repository.dashboardStatsFlow,
        repository.announcementsFlow,
        repository.friendsFlow,
        repository.logsFlow,
        repository.serverConfigFlow
    ) { flows: Array<Any?> ->
        val current = flows[0] as UiState
        val stats = flows[1] as DashboardStats
        @Suppress("UNCHECKED_CAST")
        val announcements = flows[2] as List<Announcement>
        @Suppress("UNCHECKED_CAST")
        val friends = flows[3] as List<Friend>
        @Suppress("UNCHECKED_CAST")
        val logs = flows[4] as List<SystemLog>
        val config = flows[5] as ServerConfig

        val friendsWithCount = friends
            .distinctBy { it.telegramId }
            .map { friend ->
                val count = announcements.count { it.senderTelegramId == friend.telegramId }
                friend.copy(submissionCount = count)
            }

        current.copy(
            stats = stats,
            allAnnouncements = announcements,
            friends = friendsWithCount,
            logs = logs,
            serverConfig = current.serverConfig.copy(
                serverUrl = config.serverUrl,
                apiKey = config.apiKey,
                adminTelegramId = config.adminTelegramId,
                targetChannel = config.targetChannel,
                isLocalMode = config.isLocalMode
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    fun setTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(historyFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFriendSearchQuery(query: String) {
        _uiState.update { it.copy(friendSearchQuery = query) }
    }

    // Moderation Actions
    fun approveAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            val config = _uiState.value.serverConfig
            val result = repository.approveAnnouncement(announcement.id, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "✅ Duyuru onaylandı ve @SolixxTR kanalında paylaşıldı.") }
            } else {
                _uiState.update { it.copy(toastMessage = "⚠️ Onaylama hatası: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun openRejectDialog(announcement: Announcement) {
        _uiState.update { it.copy(rejectingAnnouncement = announcement) }
    }

    fun closeRejectDialog() {
        _uiState.update { it.copy(rejectingAnnouncement = null) }
    }

    fun confirmReject(reason: String, notes: String?) {
        val ann = _uiState.value.rejectingAnnouncement ?: return
        closeRejectDialog()
        viewModelScope.launch {
            val config = _uiState.value.serverConfig
            val result = repository.rejectAnnouncement(ann.id, reason, notes, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "❌ Duyuru reddedildi ve kullanıcıya gerekçe bildirildi.") }
            } else {
                _uiState.update { it.copy(toastMessage = "⚠️ Ret işlemi başarısız: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun openEditDialog(announcement: Announcement) {
        _uiState.update { it.copy(editingAnnouncement = announcement) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(editingAnnouncement = null) }
    }

    fun confirmEdit(newText: String, notes: String?) {
        val ann = _uiState.value.editingAnnouncement ?: return
        closeEditDialog()
        viewModelScope.launch {
            val config = _uiState.value.serverConfig
            val result = repository.editAnnouncement(ann.id, newText, notes, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "✏️ Duyuru içeriği güncellendi.") }
            } else {
                _uiState.update { it.copy(toastMessage = "⚠️ Düzenleme hatası: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun openPreviewDialog(announcement: Announcement) {
        _uiState.update { it.copy(previewingAnnouncement = announcement) }
    }

    fun closePreviewDialog() {
        _uiState.update { it.copy(previewingAnnouncement = null) }
    }

    // Friend Management
    fun openAddFriendDialog() {
        _uiState.update { it.copy(showingAddFriendDialog = true) }
    }

    fun closeAddFriendDialog() {
        _uiState.update { it.copy(showingAddFriendDialog = false) }
    }

    fun confirmAddFriend(telegramId: Long, displayName: String?, username: String?, notes: String?) {
        closeAddFriendDialog()
        viewModelScope.launch {
            val config = _uiState.value.serverConfig
            val result = repository.addFriend(telegramId, displayName, username, notes, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "👤 Yetkili arkadaş başarıyla eklendi.") }
            } else {
                _uiState.update { it.copy(toastMessage = "⚠️ Arkadaş eklenemedi: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun removeFriend(telegramId: Long) {
        viewModelScope.launch {
            val config = _uiState.value.serverConfig
            val result = repository.removeFriend(telegramId, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "🗑️ Arkadaş yetkisi kaldırıldı.") }
            } else {
                _uiState.update { it.copy(toastMessage = "⚠️ Silme hatası: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun toggleFriendActive(telegramId: Long, isActive: Boolean) {
        viewModelScope.launch {
            val config = _uiState.value.serverConfig
            val result = repository.toggleFriendActive(telegramId, isActive, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = if (isActive) "Arkadaş aktif edildi" else "Arkadaş pasif yapıldı") }
            }
        }
    }

    fun viewFriendHistory(friend: Friend) {
        _uiState.update { it.copy(viewingFriendHistory = friend) }
    }

    fun closeFriendHistory() {
        _uiState.update { it.copy(viewingFriendHistory = null) }
    }

    // Server Settings & Testing
    fun updateServerSettings(
        url: String,
        apiKey: String,
        adminTgId: Long,
        targetChannel: String = "@SolixxTR",
        isLocalMode: Boolean
    ) {
        viewModelScope.launch {
            try {
                repository.saveSettings(url, apiKey, adminTgId, targetChannel, isLocalMode)
                _uiState.update {
                    it.copy(
                        serverConfig = it.serverConfig.copy(
                            serverUrl = url,
                            apiKey = apiKey,
                            adminTelegramId = adminTgId,
                            targetChannel = targetChannel,
                            isLocalMode = isLocalMode
                        ),
                        toastMessage = "💾 Ayarlar kalıcı olarak kaydedildi."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "⚠️ Ayarlar kaydedilemedi: ${e.message}") }
            }
        }
    }

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            try {
                repository.clearSettings()
                _uiState.update { it.copy(toastMessage = "Ayarlar varsayılan değerlere sıfırlandı.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "⚠️ Sıfırlama hatası: ${e.message}") }
            }
        }
    }

    fun testConnection() {
        val config = _uiState.value.serverConfig
        _uiState.update { it.copy(isTestingConnection = true) }
        viewModelScope.launch {
            val result = repository.testServerConnection(config.serverUrl, config.apiKey)
            if (result.isSuccess) {
                val (botOnline, latency) = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        serverConfig = it.serverConfig.copy(
                            isLiveConnected = true,
                            latencyMs = latency
                        ),
                        toastMessage = "🟢 Sunucuya bağlanıldı! Gecikme: ${latency}ms (Bot: ${if (botOnline) "Aktif" else "Beklemede"})"
                    )
                }
                // Automatically sync remote records when live connection succeeds
                if (!config.isLocalMode) {
                    repository.syncWithServer(config)
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Bağlantı kurulamadı"
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        serverConfig = it.serverConfig.copy(
                            isLiveConnected = false,
                            latencyMs = null
                        ),
                        toastMessage = "🔴 Sunucu bağlantı hatası: $err"
                    )
                }
            }
        }
    }

    fun syncWithServer() {
        val config = _uiState.value.serverConfig
        if (config.isLocalMode || !config.isLiveConnected) {
            _uiState.update { it.copy(toastMessage = "Canlı sunucu bağlı değil. Önce bağlantıyı test edin.") }
            return
        }
        viewModelScope.launch {
            val res = repository.syncWithServer(config)
            if (res.isSuccess) {
                _uiState.update { it.copy(toastMessage = "🔄 Sunucu ile senkronize edildi.") }
            } else {
                _uiState.update { it.copy(toastMessage = "⚠️ Senkronizasyon hatası: ${res.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _uiState.update { it.copy(toastMessage = "Sistem logları temizlendi") }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val config = _uiState.value.serverConfig
            repository.clearAllData(config)
            _uiState.update { it.copy(toastMessage = "Tüm veritabanı kayıtları temizlendi.") }
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
