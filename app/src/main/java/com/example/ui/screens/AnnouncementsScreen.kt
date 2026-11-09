package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.ui.components.AnnouncementCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberContainer
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleContainer
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.BackgroundNearBlack
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.UiState

@Composable
fun AnnouncementsScreen(
    state: UiState,
    onApprove: (Announcement) -> Unit,
    onReject: (Announcement) -> Unit,
    onEdit: (Announcement) -> Unit,
    onPreview: (Announcement) -> Unit,
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf("ALL") } // "ALL", "MEDIA", "TEXT"

    val pendingList = state.pendingAnnouncements.filter { ann ->
        val matchesType = when (selectedMediaType) {
            "MEDIA" -> ann.contentType != "TEXT"
            "TEXT" -> ann.contentType == "TEXT"
            else -> true
        }
        val matchesQuery = if (searchQuery.isBlank()) true
        else (ann.textContent?.contains(searchQuery, ignoreCase = true) == true) ||
             ann.senderDisplayName.contains(searchQuery, ignoreCase = true) ||
             (ann.senderUsername?.contains(searchQuery, ignoreCase = true) == true) ||
             ann.senderTelegramId.toString().contains(searchQuery)

        matchesType && matchesQuery
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNearBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Onay Sırası",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentAmberContainer)
                                .border(1.dp, AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${state.pendingAnnouncements.size} Bekliyor",
                                color = AccentAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Yetkili arkadaşlardan gelen duyurular onaylanmadan @SolixxTR kanalında paylaşılamaz.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Sync button
                OutlinedButton(
                    onClick = onSyncClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleLight),
                    modifier = Modifier.testTag("announcements_sync_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yenile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filter chips & Search Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Media Type Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "Tümü", "MEDIA" to "Medyalı İçerik", "TEXT" to "Sadece Metin").forEach { (code, label) ->
                        val isSelected = selectedMediaType == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentPurple else SurfaceVariantDark)
                                .border(1.dp, if (isSelected) AccentPurpleLight else BorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { selectedMediaType = code }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) BackgroundNearBlack else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Duyuru metni, kullanıcı adı veya TG ID ile ara...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("announcements_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    )
                )
            }
        }

        // Empty state
        if (pendingList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (searchQuery.isBlank()) "Onay bekleyen duyuru bulunmuyor" else "Aramaya uygun duyuru bulunamadı",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isBlank())
                                "Yetkili arkadaşlarınız Telegram botu üzerinden duyuru ilettiğinde burada listelenecektir."
                            else
                                "Farklı bir arama terimi deneyin.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(pendingList, key = { "pending_${it.id}_${it.createdAt}" }) { ann ->
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

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
