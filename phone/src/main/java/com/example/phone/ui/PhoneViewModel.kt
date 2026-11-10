package com.example.phone.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone.data.model.Announcement
import com.example.phone.data.model.ServerConfig
import com.example.phone.data.repository.SolixxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PhoneUiState(
    val pendingAnnouncements: List<Announcement> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLiveConnected: Boolean = false,
    val latencyMs: Long? = null,
    val serverConfig: ServerConfig = ServerConfig(),
    val toastMessage: String? = null,
    val rejectingAnnouncement: Announcement? = null
)

class PhoneViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SolixxRepository(application)

    private val _uiState = MutableStateFlow(PhoneUiState())
    val uiState: StateFlow<PhoneUiState> = _uiState.asStateFlow()

    init {
        // Observe persistent configuration
        viewModelScope.launch {
            repository.serverConfigFlow.collectLatest { config ->
                _uiState.update { it.copy(serverConfig = config) }
                syncPendingList()
            }
        }

        // Observe local database pending list as single source of truth
        viewModelScope.launch {
            repository.announcementsFlow.collectLatest { all ->
                val pending = all.filter { it.status == "PENDING" }
                _uiState.update { it.copy(pendingAnnouncements = pending) }
            }
        }
    }

    fun syncPendingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val config = _uiState.value.serverConfig

            // Test server connectivity
            val pingResult = repository.testServerConnection(config.serverUrl, config.apiKey)
            if (pingResult.isSuccess) {
                val (botOnline, latency) = pingResult.getOrThrow()
                _uiState.update {
                    it.copy(
                        isLiveConnected = true,
                        latencyMs = latency
                    )
                }
                // Sync latest announcements from real backend
                repository.syncWithServer(config)
            } else {
                _uiState.update {
                    it.copy(
                        isLiveConnected = false,
                        latencyMs = null
                    )
                }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun approveAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val config = _uiState.value.serverConfig
            val result = repository.approveAnnouncement(announcement.id, config)

            if (result.isSuccess) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        pendingAnnouncements = current.pendingAnnouncements.filter { it.id != announcement.id },
                        toastMessage = "✅ #${announcement.id} onaylandı ve @SolixxTR kanalında paylaşıldı."
                    )
                }
                // Trigger auto-sync in background to ensure sync state
                syncPendingList()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Onaylama işlemi başarısız oldu."
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        toastMessage = "⚠️ $errorMsg"
                    )
                }
            }
        }
    }

    fun openRejectDialog(announcement: Announcement) {
        _uiState.update { it.copy(rejectingAnnouncement = announcement) }
    }

    fun closeRejectDialog() {
        _uiState.update { it.copy(rejectingAnnouncement = null) }
    }

    fun confirmReject(reason: String) {
        val announcement = _uiState.value.rejectingAnnouncement ?: return
        closeRejectDialog()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val config = _uiState.value.serverConfig
            val result = repository.rejectAnnouncement(announcement.id, reason, null, config)

            if (result.isSuccess) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        pendingAnnouncements = current.pendingAnnouncements.filter { it.id != announcement.id },
                        toastMessage = "❌ #${announcement.id} reddedildi ve kullanıcıya bildirildi."
                    )
                }
                syncPendingList()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Ret işlemi başarısız oldu."
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        toastMessage = "⚠️ $errorMsg"
                    )
                }
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
