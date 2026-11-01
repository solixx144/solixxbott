package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddFriendDialog
import com.example.ui.components.EditDialog
import com.example.ui.components.FriendHistoryDialog
import com.example.ui.components.RejectDialog
import com.example.ui.components.SolixxTopHeader
import com.example.ui.components.TelegramPreviewDialog
import com.example.ui.screens.AnnouncementsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentPurpleContainer
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.BackgroundNearBlack
import com.example.ui.theme.SurfaceDarkCharcoal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Toast / Feedback effect
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissToast()
        }
    }

    // Back handler: if not on DASHBOARD, tapping back returns to DASHBOARD
    BackHandler(enabled = state.currentTab != AppTab.DASHBOARD) {
        viewModel.setTab(AppTab.DASHBOARD)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BackgroundNearBlack)) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                SolixxTopHeader(
                    botOnline = state.stats.botConnected,
                    targetChannel = state.stats.targetChannel,
                    isLocalMode = state.serverConfig.isLocalMode,
                    onTestClick = { viewModel.testConnection() },
                    onSyncClick = { viewModel.syncWithServer() }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    SolixxBottomNavigation(
                        currentTab = state.currentTab,
                        pendingCount = state.pendingAnnouncements.size,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = BackgroundNearBlack
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Adaptive Navigation Rail for Tablets
                if (isWideScreen) {
                    SolixxNavigationRail(
                        currentTab = state.currentTab,
                        pendingCount = state.pendingAnnouncements.size,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }

                // Main Content Screen
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (state.currentTab) {
                        AppTab.DASHBOARD -> DashboardScreen(
                            state = state,
                            onNavigateTab = { viewModel.setTab(it) },
                            onApprove = { viewModel.approveAnnouncement(it) },
                            onReject = { viewModel.openRejectDialog(it) },
                            onEdit = { viewModel.openEditDialog(it) },
                            onPreview = { viewModel.openPreviewDialog(it) },
                            onAddFriendClick = { viewModel.openAddFriendDialog() },
                            onTestPingClick = { viewModel.testConnection() }
                        )
                        AppTab.ANNOUNCEMENTS -> AnnouncementsScreen(
                            state = state,
                            onApprove = { viewModel.approveAnnouncement(it) },
                            onReject = { viewModel.openRejectDialog(it) },
                            onEdit = { viewModel.openEditDialog(it) },
                            onPreview = { viewModel.openPreviewDialog(it) }
                        )
                        AppTab.FRIENDS -> FriendsScreen(
                            state = state,
                            onSearchChange = { viewModel.setFriendSearchQuery(it) },
                            onAddFriendClick = { viewModel.openAddFriendDialog() },
                            onRemoveFriend = { viewModel.removeFriend(it) },
                            onToggleActive = { tgId, active -> viewModel.toggleFriendActive(tgId, active) },
                            onViewHistory = { viewModel.viewFriendHistory(it) }
                        )
                        AppTab.HISTORY -> HistoryScreen(
                            state = state,
                            onFilterChange = { viewModel.setHistoryFilter(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onPreview = { viewModel.openPreviewDialog(it) }
                        )
                        AppTab.SETTINGS -> SettingsScreen(
                            state = state,
                            onSaveSettings = { url, key, adminId, channel, local ->
                                viewModel.updateServerSettings(url, key, adminId, channel, local)
                            },
                            onTestConnection = { viewModel.testConnection() },
                            onClearLogs = { viewModel.clearLogs() },
                            onClearAllData = { viewModel.clearAllData() }
                        )
                    }
                }
            }
        }
    }

    // Dialog Modals
    state.rejectingAnnouncement?.let { ann ->
        RejectDialog(
            announcement = ann,
            onDismiss = { viewModel.closeRejectDialog() },
            onConfirm = { reason, notes -> viewModel.confirmReject(reason, notes) }
        )
    }

    state.editingAnnouncement?.let { ann ->
        EditDialog(
            announcement = ann,
            onDismiss = { viewModel.closeEditDialog() },
            onConfirm = { newText, notes -> viewModel.confirmEdit(newText, notes) }
        )
    }

    state.previewingAnnouncement?.let { ann ->
        TelegramPreviewDialog(
            announcement = ann,
            onDismiss = { viewModel.closePreviewDialog() },
            onApproveDirectly = {
                viewModel.closePreviewDialog()
                viewModel.approveAnnouncement(ann)
            }
        )
    }

    if (state.showingAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { viewModel.closeAddFriendDialog() },
            onConfirm = { tgId, name, uname, notes ->
                viewModel.confirmAddFriend(tgId, name, uname, notes)
            }
        )
    }

    state.viewingFriendHistory?.let { friend ->
        val friendSubmissions = state.allAnnouncements.filter { it.senderTelegramId == friend.telegramId }
        FriendHistoryDialog(
            friend = friend,
            submissions = friendSubmissions,
            onDismiss = { viewModel.closeFriendHistory() }
        )
    }
}

@Composable
fun SolixxBottomNavigation(
    currentTab: AppTab,
    pendingCount: Int,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDarkCharcoal,
        contentColor = TextSecondary,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val items = listOf(
            Triple(AppTab.DASHBOARD, Icons.Default.Dashboard, "Panel"),
            Triple(AppTab.ANNOUNCEMENTS, Icons.Default.HourglassTop, "Onay Sırası"),
            Triple(AppTab.FRIENDS, Icons.Default.People, "Arkadaşlar"),
            Triple(AppTab.HISTORY, Icons.Default.History, "Geçmiş"),
            Triple(AppTab.SETTINGS, Icons.Default.Settings, "Ayarlar")
        )

        items.forEach { (tab, icon, label) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == AppTab.ANNOUNCEMENTS && pendingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = AccentAmber,
                                    contentColor = BackgroundNearBlack
                                ) {
                                    Text(text = "$pendingCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(imageVector = icon, contentDescription = label)
                        }
                    } else {
                        Icon(imageVector = icon, contentDescription = label)
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentPurpleLight,
                    selectedTextColor = AccentPurpleLight,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = AccentPurpleContainer
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun SolixxNavigationRail(
    currentTab: AppTab,
    pendingCount: Int,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationRail(
        containerColor = SurfaceDarkCharcoal,
        contentColor = TextSecondary,
        modifier = Modifier.width(80.dp)
    ) {
        val items = listOf(
            Triple(AppTab.DASHBOARD, Icons.Default.Dashboard, "Panel"),
            Triple(AppTab.ANNOUNCEMENTS, Icons.Default.HourglassTop, "Onay Sırası"),
            Triple(AppTab.FRIENDS, Icons.Default.People, "Arkadaşlar"),
            Triple(AppTab.HISTORY, Icons.Default.History, "Geçmiş"),
            Triple(AppTab.SETTINGS, Icons.Default.Settings, "Ayarlar")
        )

        Spacer(modifier = Modifier.height(16.dp))

        items.forEach { (tab, icon, label) ->
            val isSelected = currentTab == tab
            NavigationRailItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == AppTab.ANNOUNCEMENTS && pendingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = AccentAmber,
                                    contentColor = BackgroundNearBlack
                                ) {
                                    Text(text = "$pendingCount", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(imageVector = icon, contentDescription = label)
                        }
                    } else {
                        Icon(imageVector = icon, contentDescription = label)
                    }
                },
                label = { Text(text = label, fontSize = 10.sp) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = AccentPurpleLight,
                    selectedTextColor = AccentPurpleLight,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = AccentPurpleContainer
                )
            )
        }
    }
}
