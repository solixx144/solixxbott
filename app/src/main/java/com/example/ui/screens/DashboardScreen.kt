package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.ui.components.AnnouncementCard
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberContainer
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentEmeraldContainer
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleContainer
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentRoseContainer
import com.example.ui.theme.BackgroundNearBlack
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.UiState

@Composable
fun DashboardScreen(
    state: UiState,
    onNavigateTab: (AppTab) -> Unit,
    onApprove: (Announcement) -> Unit,
    onReject: (Announcement) -> Unit,
    onEdit: (Announcement) -> Unit,
    onPreview: (Announcement) -> Unit,
    onAddFriendClick: () -> Unit,
    onTestPingClick: () -> Unit,
    onSyncClick: () -> Unit = {},
    onOpenErrorLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNearBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Dashboard Welcome Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (state.serverConfig.adminTelegramId > 0)
                                    "Yönetici Paneli (TG ID: ${state.serverConfig.adminTelegramId})"
                                else
                                    "Yönetici Kontrol Paneli",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "SolixxTR Kanalı İçerik & Onay Yönetim Merkezi",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AccentPurpleContainer)
                                .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ADMİN",
                                color = AccentPurpleLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Live Server & Telegram Bot Status Cards (Real API Connection Status)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Sunucu Durumu Kartı
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .testTag("server_status_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (state.serverConfig.isLiveConnected) AccentEmeraldContainer
                                            else if (state.serverConfig.isLocalMode) AccentAmberContainer
                                            else AccentRoseContainer
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = "Server",
                                        tint = if (state.serverConfig.isLiveConnected) AccentEmerald
                                        else if (state.serverConfig.isLocalMode) AccentAmber
                                        else AccentRose,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sunucu Durumu",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (state.serverConfig.isLiveConnected) AccentEmeraldContainer
                                        else if (state.serverConfig.isLocalMode) AccentAmberContainer
                                        else AccentRoseContainer
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (state.serverConfig.isLiveConnected) "CANLI"
                                    else if (state.serverConfig.isLocalMode) "YEREL"
                                    else "KAPALI",
                                    color = if (state.serverConfig.isLiveConnected) AccentEmerald
                                    else if (state.serverConfig.isLocalMode) AccentAmber
                                    else AccentRose,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = state.serverConfig.serverUrl.replace("http://", "").replace("https://", ""),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.serverConfig.latencyMs != null) "Gecikme: ${state.serverConfig.latencyMs}ms" else "SQLite WAL Aktif",
                                color = if (state.serverConfig.latencyMs != null) AccentEmerald else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentPurpleContainer)
                                    .clickable { onTestPingClick() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = AccentPurpleLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (state.isTestingConnection) "..." else "Ping",
                                        color = AccentPurpleLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Telegram Bot Bağlantısı Kartı
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .testTag("bot_status_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(TelegramBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Bot",
                                        tint = TelegramBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Telegram Bot",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (state.stats.botConnected) AccentEmeraldContainer else AccentRoseContainer)
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (state.stats.botConnected) "AKTİF" else "BEKLEMEDE",
                                    color = if (state.stats.botConnected) AccentEmerald else AccentRose,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = TelegramBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.stats.targetChannel.ifBlank { "@SolixxTR" },
                                color = TelegramBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (state.serverConfig.adminTelegramId > 0) "Yönetici ID: ${state.serverConfig.adminTelegramId}" else "Admin ID Girilmeli",
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Centralized Error Logger & Diagnostics Card
        item {
            val errorCount = state.logs.count { it.level == "ERROR" }
            val networkCount = state.logs.count { it.category == "NETWORK" }
            val apiCount = state.logs.count { it.category == "API" }
            val jsonCount = state.logs.count { it.category == "JSON_PARSE" }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (errorCount > 0) AccentRose.copy(alpha = 0.5f) else BorderSubtle,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onOpenErrorLogs() }
                    .testTag("error_logger_dashboard_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (errorCount > 0) AccentRoseContainer else AccentEmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (errorCount > 0) Icons.Default.BugReport else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (errorCount > 0) AccentRose else AccentEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sistem & Hata İzleme Günlüğü",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (errorCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AccentRose)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$errorCount HATA",
                                            color = BackgroundNearBlack,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (errorCount > 0)
                                    "Ağ: $networkCount • API: $apiCount • JSON: $jsonCount (İncelemek için dokunun)"
                                else
                                    "Tüm API, ağ ve JSON akışları sorunsuz çalışıyor",
                                color = if (errorCount > 0) AccentRose.copy(alpha = 0.9f) else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentPurpleContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Görüntüle", color = AccentPurpleLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4 Key Metric Cards (Strictly calculated from real database)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Bekleyen Onay",
                    value = "${state.stats.pendingAnnouncements}",
                    subtitle = "İncelenmeyi bekliyor",
                    icon = Icons.Default.HourglassTop,
                    accentColor = AccentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(AppTab.ANNOUNCEMENTS) }
                )

                MetricStatCard(
                    title = "Yayımlanan",
                    value = "${state.stats.publishedAnnouncements}",
                    subtitle = "@SolixxTR kanalında",
                    icon = Icons.Default.CheckCircle,
                    accentColor = AccentEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(AppTab.HISTORY) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Reddedilen",
                    value = "${state.stats.rejectedAnnouncements}",
                    subtitle = "Gerekçe bildirildi",
                    icon = Icons.Default.Close,
                    accentColor = AccentRose,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(AppTab.HISTORY) }
                )

                MetricStatCard(
                    title = "Yetkili Arkadaşlar",
                    value = "${state.stats.totalFriends}",
                    subtitle = "Duyuru gönderebilir",
                    icon = Icons.Default.People,
                    accentColor = AccentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(AppTab.FRIENDS) }
                )
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Hızlı İşlemler",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action 1: Onay Sırasına Git
                Button(
                    onClick = { onNavigateTab(AppTab.ANNOUNCEMENTS) },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("dashboard_pending_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Onay Sırası (${state.stats.pendingAnnouncements})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Action 2: Arkadaş Ekle
                OutlinedButton(
                    onClick = onAddFriendClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_add_friend_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Arkadaş Ekle", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Action 3: Sunucu Ping Testi
                OutlinedButton(
                    onClick = onTestPingClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_ping_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.serverConfig.latencyMs != null) "${state.serverConfig.latencyMs}ms" else "Test",
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Pending Queue Preview Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bekleyen Onaylar",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentAmber)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${state.pendingAnnouncements.size}",
                            color = BackgroundNearBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = "Tümünü Gör",
                    color = AccentPurpleLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateTab(AppTab.ANNOUNCEMENTS) }
                )
            }
        }

        if (state.pendingAnnouncements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Onay bekleyen duyuru bulunmuyor",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Yetkili arkadaşlarınız Telegram'dan gönderim yaptığında burada görünecektir.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(state.pendingAnnouncements.take(2)) { ann ->
                AnnouncementCard(
                    announcement = ann,
                    onApprove = onApprove,
                    onReject = onReject,
                    onEdit = onEdit,
                    onPreview = onPreview,
                    serverUrl = state.serverConfig.serverUrl
                )
            }
        }

        // Recent System Activity Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Son Sistem Aktiviteleri",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tüm Loglar",
                    color = AccentPurpleLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateTab(AppTab.SETTINGS) }
                )
            }
        }

        if (state.logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz bir sistem aktivitesi kaydedilmedi.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(state.logs.take(4)) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dotColor = when (log.level) {
                            "SUCCESS" -> AccentEmerald
                            "WARN" -> AccentAmber
                            "ERROR" -> AccentRose
                            else -> AccentPurpleLight
                        }

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.message,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "[${log.category}]",
                                    color = AccentPurpleLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.timestamp,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
