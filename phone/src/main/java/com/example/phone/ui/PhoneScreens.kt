package com.example.phone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phone.data.model.Announcement
import com.example.phone.data.model.Friend
import com.example.phone.ui.theme.AccentAmber
import com.example.phone.ui.theme.AccentAmberContainer
import com.example.phone.ui.theme.AccentEmerald
import com.example.phone.ui.theme.AccentEmeraldContainer
import com.example.phone.ui.theme.AccentPurple
import com.example.phone.ui.theme.AccentPurpleContainer
import com.example.phone.ui.theme.AccentPurpleLight
import com.example.phone.ui.theme.AccentRose
import com.example.phone.ui.theme.AccentRoseContainer
import com.example.phone.ui.theme.BorderSubtle
import com.example.phone.ui.theme.SurfaceCard
import com.example.phone.ui.theme.SurfaceDarkCharcoal
import com.example.phone.ui.theme.SurfaceVariantDark
import com.example.phone.ui.theme.TelegramBlue
import com.example.phone.ui.theme.TextMuted
import com.example.phone.ui.theme.TextPrimary
import com.example.phone.ui.theme.TextSecondary

@Composable
fun PhoneQueueScreen(
    state: PhoneUiState,
    onApprove: (Announcement) -> Unit,
    onReject: (Announcement) -> Unit,
    onEdit: (Announcement) -> Unit,
    onPreview: (Announcement) -> Unit,
    onFilterChange: (String) -> Unit
) {
    val filteredQueue = remember(state.pendingAnnouncements, state.mediaFilter) {
        when (state.mediaFilter) {
            "PHOTO" -> state.pendingAnnouncements.filter { it.contentType == "PHOTO" }
            "ALBUM" -> state.pendingAnnouncements.filter { it.contentType == "ALBUM" }
            "TEXT" -> state.pendingAnnouncements.filter { it.contentType == "TEXT" }
            else -> state.pendingAnnouncements
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Queue Header Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Onay Sırası",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Yetkili arkadaşlardan gelen gönderimler",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentAmberContainer)
                        .border(1.dp, AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${state.pendingAnnouncements.size} Bekliyor",
                        color = AccentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "Tümü (${state.pendingAnnouncements.size})",
                    "PHOTO" to "Fotoğraf",
                    "ALBUM" to "Albüm",
                    "TEXT" to "Metin"
                ).forEach { (key, label) ->
                    val isSelected = state.mediaFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AccentPurple else SurfaceVariantDark)
                            .border(1.dp, if (isSelected) AccentPurpleLight else BorderSubtle, RoundedCornerShape(16.dp))
                            .clickable { onFilterChange(key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Empty state
        if (filteredQueue.isEmpty()) {
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
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Onay bekleyen duyuru yok", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Yetkili arkadaşlarınız Telegram'dan gönderim yaptığında anında burada listelenir.", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredQueue, key = { "q_${it.id}_${it.createdAt}" }) { ann ->
                PhoneAnnouncementCard(
                    announcement = ann,
                    serverUrl = state.serverConfig.serverUrl,
                    onApprove = { onApprove(ann) },
                    onReject = { onReject(ann) },
                    onEdit = { onEdit(ann) },
                    onPreview = { onPreview(ann) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun PhoneHistoryScreen(
    state: PhoneUiState,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onPreview: (Announcement) -> Unit
) {
    val filteredHistory = remember(state.allAnnouncements, state.historyStatusFilter, state.searchQuery) {
        state.allAnnouncements.filter { ann ->
            val matchStatus = when (state.historyStatusFilter) {
                "APPROVED" -> ann.status == "APPROVED"
                "REJECTED" -> ann.status == "REJECTED"
                else -> true
            }
            val matchSearch = if (state.searchQuery.isBlank()) true else {
                ann.senderDisplayName.contains(state.searchQuery, ignoreCase = true) ||
                (ann.senderUsername?.contains(state.searchQuery, ignoreCase = true) == true) ||
                (ann.textContent?.contains(state.searchQuery, ignoreCase = true) == true) ||
                ann.senderTelegramId.toString().contains(state.searchQuery)
            }
            matchStatus && matchSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Duyuru Arşivi", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("Yayımlanan ve reddedilen tüm gönderimler", color = TextMuted, fontSize = 11.sp)
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Duyuru veya kullanıcı ara...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "Tümü (${state.allAnnouncements.size})",
                    "APPROVED" to "Yayımlanan (${state.stats.publishedAnnouncements})",
                    "REJECTED" to "Reddedilen (${state.stats.rejectedAnnouncements})"
                ).forEach { (key, label) ->
                    val isSel = state.historyStatusFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) AccentPurple else SurfaceVariantDark)
                            .border(1.dp, if (isSel) AccentPurpleLight else BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable { onStatusFilterChange(key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = label, color = if (isSel) Color.White else TextSecondary, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        if (filteredHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Kayıt bulunamadı", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredHistory, key = { "hist_${it.id}_${it.createdAt}" }) { ann ->
                PhoneAnnouncementCard(
                    announcement = ann,
                    serverUrl = state.serverConfig.serverUrl,
                    onApprove = {},
                    onReject = {},
                    onEdit = {},
                    onPreview = { onPreview(ann) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun PhoneFriendsScreen(
    state: PhoneUiState,
    onAddFriendClick: () -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onDeleteFriend: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredFriends = remember(state.friends, searchQuery) {
        if (searchQuery.isBlank()) state.friends else {
            state.friends.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                (it.username?.contains(searchQuery, ignoreCase = true) == true) ||
                it.telegramId.toString().contains(searchQuery)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Yetkili Arkadaşlar", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("Yalnızca bu listedekiler duyuru gönderebilir", color = TextMuted, fontSize = 11.sp)
                }
                Button(
                    onClick = onAddFriendClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("İsim veya Telegram ID ara...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        if (filteredFriends.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Yetkili arkadaş bulunmuyor", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredFriends, key = { "fr_${it.telegramId}" }) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (friend.isActive) AccentPurpleContainer else SurfaceVariantDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.displayName.take(1).uppercase().ifEmpty { "U" },
                                color = if (friend.isActive) AccentPurpleLight else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = friend.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "TG ID: ${friend.telegramId}${if (!friend.username.isNullOrBlank()) " • @${friend.username}" else ""}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            if (!friend.notes.isNullOrBlank()) {
                                Text(text = "Not: ${friend.notes}", color = TextSecondary, fontSize = 10.sp, maxLines = 1)
                            }
                        }

                        // Active switch
                        Switch(
                            checked = friend.isActive,
                            onCheckedChange = { onToggleActive(friend.telegramId, friend.isActive) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentEmerald,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceVariantDark
                            )
                        )

                        // Delete button
                        IconButton(onClick = { onDeleteFriend(friend.telegramId) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = AccentRose, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun PhoneSettingsScreen(
    state: PhoneUiState,
    onSaveSettings: (url: String, apiKey: String, adminTgId: Long, channel: String, isLocalMode: Boolean) -> Unit,
    onSyncClick: () -> Unit,
    onClearAllData: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(state.serverConfig.serverUrl) }
    var apiKey by remember { mutableStateOf(state.serverConfig.apiKey) }
    var adminTgIdText by remember { mutableStateOf(if (state.serverConfig.adminTelegramId > 0) state.serverConfig.adminTelegramId.toString() else "") }
    var channel by remember { mutableStateOf(state.serverConfig.targetChannel) }
    var isLocalMode by remember { mutableStateOf(state.serverConfig.isLocalMode) }

    var showClearConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Sistem & Ayarlar", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("Sunucu ve bot bağlantı parametreleri kalıcıdır", color = TextMuted, fontSize = 11.sp)
        }

        // Connection Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bağlantı Durumu", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (state.serverConfig.isLiveConnected) AccentEmeraldContainer else AccentRoseContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.serverConfig.isLiveConnected) "CANLI SUNUCU BAĞLI" else "SUNUCU BAĞLANTISI YOK",
                                color = if (state.serverConfig.isLiveConnected) AccentEmerald else AccentRose,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Telegram Bot (@SolixxTR):", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = if (state.stats.botConnected) "Aktif (Polling Çalışıyor)" else "Beklemede / Çevrimdışı",
                            color = if (state.stats.botConnected) AccentEmerald else AccentAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gecikme Süresi (Ping):", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = state.serverConfig.latencyMs?.let { "${it}ms" } ?: "Ölçülmedi",
                            color = if (state.serverConfig.latencyMs != null) AccentEmerald else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onSyncClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleLight)
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sunucu Bağlantısını Test Et & Eşitle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Configuration Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Kalıcı Yapılandırma", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Yerel / Canlı Mod", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (isLocalMode) "Yerel SQLite öncelikli" else "Doğrudan canlı backend API", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = !isLocalMode,
                            onCheckedChange = { isLocalMode = !it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Backend Sunucu Adresi:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Yönetici API Anahtarı (X-Admin-Key):", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        placeholder = { Text("ADMIN_API_KEY", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Yönetici Telegram ID (X-Admin-Telegram-Id):", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = adminTgIdText,
                        onValueChange = { adminTgIdText = it.filter { c -> c.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Hedef Telegram Kanalı:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = channel,
                        onValueChange = { channel = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val tid = adminTgIdText.toLongOrNull() ?: 0L
                            onSaveSettings(serverUrl, apiKey, tid, channel, isLocalMode)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ayarları Kalıcı Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Diagnostic Logs Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Tanılama & Hata Günlüğü (${state.logs.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.logs.isEmpty()) {
                        Text("Günlük kaydı bulunmuyor.", color = TextMuted, fontSize = 11.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.logs.take(5).forEach { log ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val tint = when (log.level) {
                                        "ERROR" -> AccentRose
                                        "WARN" -> AccentAmber
                                        "SUCCESS" -> AccentEmerald
                                        else -> AccentPurpleLight
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(tint)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "[${log.category}] ${log.message}", color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = log.timestamp, color = TextMuted, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clear Data Danger Zone
        item {
            OutlinedButton(
                onClick = { showClearConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bütün Verileri Temizle", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = SurfaceDarkCharcoal,
            title = { Text("Verileri Temizle?", color = AccentRose, fontWeight = FontWeight.Bold) },
            text = { Text("Tüm duyurular, yetkili arkadaşlar ve günlükler sıfırlanacaktır. Bu işlem geri alınamaz.", color = TextPrimary, fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                ) {
                    Text("Evet, Hepsini Sil")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirm = false }) { Text("Vazgeç") }
            }
        )
    }
}
