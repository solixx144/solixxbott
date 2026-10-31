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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Friend
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
import com.example.ui.viewmodel.UiState

@Composable
fun FriendsScreen(
    state: UiState,
    onSearchChange: (String) -> Unit,
    onAddFriendClick: () -> Unit,
    onRemoveFriend: (Long) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onViewHistory: (Friend) -> Unit,
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
            // Section Title & Add Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Yetkili Arkadaşlar",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentPurpleContainer)
                                .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${state.friends.size} Kişi",
                                color = AccentPurpleLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Telegram üzerinden duyuru gönderme iznine sahip kullanıcılar",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onAddFriendClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_friend_button")
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekle", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Security Info Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AccentPurpleLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Güvenlik Koruması: Listede yer almayan yabancı Telegram kullanıcıları bota mesaj gönderdiğinde içerikleri reddedilir ve kanala iletilmez.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = state.friendSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("İsim, @kullanıcı veya TG ID ile ara...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("friend_search_input"),
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

        // Friends List
        if (state.filteredFriends.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.friendSearchQuery.isBlank()) "Henüz yetkili arkadaş eklenmedi." else "Aramaya uygun arkadaş bulunamadı.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(state.filteredFriends, key = { it.telegramId }) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .testTag("friend_item_${friend.telegramId}"),
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (friend.isActive) AccentPurpleContainer else Color(0xFF2B2D38)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = friend.displayName.take(1).uppercase(),
                                        color = if (friend.isActive) AccentPurpleLight else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = friend.displayName,
                                        color = if (friend.isActive) TextPrimary else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!friend.username.isNullOrBlank()) {
                                            Text(
                                                text = "@${friend.username}",
                                                color = TelegramBlue,
                                                fontSize = 12.sp
                                            )
                                        } else {
                                            Text(
                                                text = "Username unavailable",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("•", color = TextMuted, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ID: ${friend.telegramId}",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Active / Inactive Toggle Switch
                            Switch(
                                checked = friend.isActive,
                                onCheckedChange = { onToggleActive(friend.telegramId, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AccentPurple,
                                    checkedTrackColor = AccentPurpleContainer,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = SurfaceVariantDark
                                )
                            )
                        }

                        if (!friend.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Not: ${friend.notes}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Eklendi: ${friend.addedAt.take(10)}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // View Submissions
                                OutlinedButton(
                                    onClick = { onViewHistory(friend) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleLight),
                                    modifier = Modifier.testTag("friend_history_${friend.telegramId}")
                                ) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Geçmiş (${friend.submissionCount})", fontSize = 11.sp)
                                }

                                // Remove Authorization
                                OutlinedButton(
                                    onClick = { onRemoveFriend(friend.telegramId) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AccentRose,
                                        containerColor = AccentRoseContainer.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag("friend_delete_${friend.telegramId}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Yetkiyi Kaldır", fontSize = 11.sp)
                                }
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
