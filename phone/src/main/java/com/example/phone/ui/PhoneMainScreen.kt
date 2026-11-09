package com.example.phone.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phone.ui.theme.BackgroundNearBlack

@Composable
fun PhoneMainScreen(viewModel: PhoneViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        containerColor = BackgroundNearBlack,
        topBar = {
            PhoneTopHeader(
                isLiveConnected = state.serverConfig.isLiveConnected,
                latencyMs = state.serverConfig.latencyMs,
                targetChannel = state.serverConfig.targetChannel,
                onSyncClick = { viewModel.syncData() }
            )
        },
        bottomBar = {
            PhoneBottomNavBar(
                currentTab = state.currentTab,
                pendingCount = state.pendingAnnouncements.size,
                onTabSelected = { viewModel.setTab(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentTab) {
                PhoneTab.QUEUE -> PhoneQueueScreen(
                    state = state,
                    onApprove = { viewModel.approveAnnouncement(it) },
                    onReject = { viewModel.openRejectDialog(it) },
                    onEdit = { viewModel.openEditDialog(it) },
                    onPreview = { viewModel.openPreviewDialog(it) },
                    onFilterChange = { viewModel.setMediaFilter(it) }
                )
                PhoneTab.HISTORY -> PhoneHistoryScreen(
                    state = state,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onStatusFilterChange = { viewModel.setHistoryStatusFilter(it) },
                    onPreview = { viewModel.openPreviewDialog(it) }
                )
                PhoneTab.FRIENDS -> PhoneFriendsScreen(
                    state = state,
                    onAddFriendClick = { viewModel.openAddFriendDialog() },
                    onToggleActive = { tgId, act -> viewModel.toggleFriendActive(tgId, act) },
                    onDeleteFriend = { viewModel.removeFriend(it) }
                )
                PhoneTab.SETTINGS -> PhoneSettingsScreen(
                    state = state,
                    onSaveSettings = { url, key, tgId, ch, isLocal ->
                        viewModel.saveSettings(url, key, tgId, ch, isLocal)
                    },
                    onSyncClick = { viewModel.syncData() },
                    onClearAllData = { viewModel.clearAllData() }
                )
            }
        }
    }

    // Modals
    state.rejectingAnnouncement?.let { ann ->
        PhoneRejectDialog(
            announcement = ann,
            onDismiss = { viewModel.closeRejectDialog() },
            onConfirm = { reason, notes -> viewModel.confirmReject(reason, notes) }
        )
    }

    state.editingAnnouncement?.let { ann ->
        PhoneEditDialog(
            announcement = ann,
            serverUrl = state.serverConfig.serverUrl,
            onDismiss = { viewModel.closeEditDialog() },
            onConfirm = { newText, notes, mediaItems ->
                viewModel.confirmEdit(newText, notes, mediaItems)
            }
        )
    }

    state.previewingAnnouncement?.let { ann ->
        PhonePreviewDialog(
            announcement = ann,
            serverUrl = state.serverConfig.serverUrl,
            onDismiss = { viewModel.closePreviewDialog() },
            onApproveDirectly = {
                viewModel.closePreviewDialog()
                viewModel.approveAnnouncement(ann)
            }
        )
    }

    if (state.showAddFriendDialog) {
        PhoneAddFriendDialog(
            onDismiss = { viewModel.closeAddFriendDialog() },
            onConfirm = { tgId, name, uname, notes ->
                viewModel.confirmAddFriend(tgId, name, uname, notes)
            }
        )
    }
}
