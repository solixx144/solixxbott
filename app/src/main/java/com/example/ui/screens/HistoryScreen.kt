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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.ui.components.AnnouncementCard
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleContainer
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BackgroundNearBlack
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HistoryFilter
import com.example.ui.viewmodel.UiState

@Composable
fun HistoryScreen(
    state: UiState,
    onFilterChange: (HistoryFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onPreview: (Announcement) -> Unit,
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
            // Section Title
            Column {
                Text(
                    text = "Duyuru Geçmişi & Arşiv",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Text(
                    text = "Tüm yayımlanan, reddedilen ve arşivlenen başvuruların kalıcı kayıtları",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Filter Tabs Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilter.entries.forEach { filter ->
                    val isSelected = state.historyFilter == filter
                    val count = when (filter) {
                        HistoryFilter.ALL -> state.allAnnouncements.size
                        HistoryFilter.PUBLISHED -> state.publishedAnnouncements.size
                        HistoryFilter.REJECTED -> state.rejectedAnnouncements.size
                        HistoryFilter.PENDING -> state.pendingAnnouncements.size
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AccentPurpleContainer else SurfaceVariantDark)
                            .border(1.dp, if (isSelected) AccentPurple else BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onFilterChange(filter) }
                            .padding(vertical = 8.dp)
                            .testTag("history_filter_${filter.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = filter.label,
                                color = if (isSelected) AccentPurpleLight else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "$count",
                                color = if (isSelected) AccentPurpleLight else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Geçmişte metin, yazar veya kullanıcı ile ara...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
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

        // Announcements List
        if (state.filteredHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bu filtreye ait kayıt bulunamadı.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(state.filteredHistory, key = { it.id }) { ann ->
                AnnouncementCard(
                    announcement = ann,
                    onApprove = {},
                    onReject = {},
                    onEdit = {},
                    onPreview = onPreview
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
