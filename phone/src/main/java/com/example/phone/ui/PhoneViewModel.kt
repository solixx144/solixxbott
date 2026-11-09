package com.example.phone.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone.data.model.Announcement
import com.example.phone.data.model.DashboardStats
import com.example.phone.data.model.Friend
import com.example.phone.data.model.MediaItem
import com.example.phone.data.model.ServerConfig
import com.example.phone.data.model.SystemLog
import com.example.phone.data.repository.SolixxRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PhoneTab(val title: String) {
    QUEUE("Onay Sırası"),
    HISTORY("Duyurular"),
    FRIENDS("Arkadaşlar"),
    SETTINGS("Sistem & Ayarlar")
}

data class PhoneUiState(
    val currentTab: PhoneTab = PhoneTab.QUEUE,
    val pendingAnnouncements: List<Announcement> = emptyList(),
    val allAnnouncements: List<Announcement> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val logs: List<SystemLog> = emptyList(),
    val stats: DashboardStats = DashboardStats(),
    val serverConfig: ServerConfig = ServerConfig(),
    val isRefreshing: Boolean = false,
    val toastMessage: String? = null,
    val searchQuery: String = "",
    val mediaFilter: String = "ALL", // "ALL", "PHOTO", "ALBUM", "TEXT"
    val historyStatusFilter: String = "ALL", // "ALL", "APPROVED", "REJECTED"
    val rejectingAnnouncement: Announcement? = null,
    val editingAnnouncement: Announcement? = null,
    val previewingAnnouncement: Announcement? = null,
    val showAddFriendDialog: Boolean = false
)

class PhoneViewModel(application: Application) : AndroidViewModel(application) {
    val repository = SolixxRepository(application)

    private val _uiState = MutableStateFlow(PhoneUiState())
    val uiState: StateFlow<PhoneUiState> = _uiState

    init {
        // Collect Server Config
        viewModelScope.launch {
            repository.serverConfigFlow.collectLatest { config ->
                _uiState.update { it.copy(serverConfig = config) }
            }
        }

        // Collect Announcements
        viewModelScope.launch {
            repository.announcementsFlow.collectLatest { list ->
                val pending = list.filter { it.status == "PENDING" }
                val approved = list.filter { it.status == "APPROVED" }
                val rejected = list.filter { it.status == "REJECTED" }
                _uiState.update { current ->
                    current.copy(
                        allAnnouncements = list,
                        pendingAnnouncements = pending,
                        stats = current.stats.copy(
                            totalAnnouncements = list.size,
                            pendingAnnouncements = pending.size,
                            publishedAnnouncements = approved.size,
                            rejectedAnnouncements = rejected.size
                        )
                    )
                }
            }
        }

        // Collect Friends
        viewModelScope.launch {
            repository.friendsFlow.collectLatest { list ->
                _uiState.update { current ->
                    current.copy(
                        friends = list,
                        stats = current.stats.copy(totalFriends = list.size)
                    )
                }
            }
        }

        // Collect Logs
        viewModelScope.launch {
            repository.logsFlow.collectLatest { list ->
                _uiState.update { it.copy(logs = list) }
            }
        }

        // Initial sync & auto-ping
        viewModelScope.launch {
            delay(500)
            syncData()
        }
    }

    fun setTab(tab: PhoneTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setMediaFilter(filter: String) {
        _uiState.update { it.copy(mediaFilter = filter) }
    }

    fun setHistoryStatusFilter(filter: String) {
        _uiState.update { it.copy(historyStatusFilter = filter) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val config = uiState.value.serverConfig
            val pingResult = repository.testServerConnection(config.serverUrl, config.apiKey)
            if (pingResult.isSuccess) {
                val (botOnline, latency) = pingResult.getOrThrow()
                _uiState.update {
                    it.copy(
                        serverConfig = it.serverConfig.copy(isLiveConnected = true, latencyMs = latency),
                        stats = it.stats.copy(botConnected = botOnline)
                    )
                }
                repository.syncWithServer(config)
            } else {
                _uiState.update {
                    it.copy(
                        serverConfig = it.serverConfig.copy(isLiveConnected = false, latencyMs = null)
                    )
                }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun approveAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            val config = uiState.value.serverConfig
            val result = repository.approveAnnouncement(announcement.id, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "✅ Duyuru onaylandı ve @SolixxTR kanalında paylaşıldı!") }
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
            val config = uiState.value.serverConfig
            val result = repository.rejectAnnouncement(ann.id, reason, notes, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "❌ Duyuru reddedildi ve kullanıcıya iletildi.") }
            } else {
                _uiState.update { it.copy(toastMessage = "⚠️ Ret hatası: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun openEditDialog(announcement: Announcement) {
        _uiState.update { it.copy(editingAnnouncement = announcement) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(editingAnnouncement = null) }
    }

    fun confirmEdit(newText: String, notes: String?, mediaItems: List<MediaItem>? = null) {
        val ann = _uiState.value.editingAnnouncement ?: return
        closeEditDialog()
        viewModelScope.launch {
            val config = uiState.value.serverConfig
            val result = repository.editAnnouncement(ann.id, newText, notes, mediaItems, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "✏️ Duyuru metni ve fotoğrafları güncellendi.") }
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

    fun openAddFriendDialog() {
        _uiState.update { it.copy(showAddFriendDialog = true) }
    }

    fun closeAddFriendDialog() {
        _uiState.update { it.copy(showAddFriendDialog = false) }
    }

    fun confirmAddFriend(telegramId: Long, name: String?, username: String?, notes: String?) {
        closeAddFriendDialog()
        viewModelScope.launch {
            val config = uiState.value.serverConfig
            val result = repository.addFriend(telegramId, name, username, notes, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "👤 Yetkili arkadaş başarıyla eklendi.") }
            } else {
                _uiState.update { it.copy(toastMessage = "⚠️ Arkadaş ekleme hatası: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun toggleFriendActive(telegramId: Long, currentActive: Boolean) {
        viewModelScope.launch {
            val config = uiState.value.serverConfig
            val result = repository.toggleFriendActive(telegramId, !currentActive, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = if (!currentActive) "Yetki aktif edildi." else "Yetki askıya alındı.") }
            }
        }
    }

    fun removeFriend(telegramId: Long) {
        viewModelScope.launch {
            val config = uiState.value.serverConfig
            val result = repository.removeFriend(telegramId, config)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "Arkadaş yetkisi silindi.") }
            }
        }
    }

    fun saveSettings(url: String, apiKey: String, adminTgId: Long, channel: String, isLocalMode: Boolean) {
        viewModelScope.launch {
            repository.saveSettings(
                serverUrl = url.trim().trimEnd('/'),
                apiKey = apiKey.trim(),
                adminTelegramId = adminTgId,
                targetChannel = channel.trim(),
                isLocalMode = isLocalMode
            )
            _uiState.update { it.copy(toastMessage = "💾 Ayarlar kalıcı olarak kaydedildi.") }
            syncData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val config = uiState.value.serverConfig
            repository.clearAllData(config)
            _uiState.update { it.copy(toastMessage = "🗑️ Bütün yerel ve sunucu verileri temizlendi.") }
        }
    }
}
