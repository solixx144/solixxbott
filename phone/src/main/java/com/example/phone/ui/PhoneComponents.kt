package com.example.phone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.phone.data.model.Announcement
import com.example.phone.data.model.MediaItem
import com.example.phone.ui.theme.AccentAmber
import com.example.phone.ui.theme.AccentEmerald
import com.example.phone.ui.theme.AccentEmeraldContainer
import com.example.phone.ui.theme.AccentIndigo
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

fun resolvePhoneMediaUrl(item: MediaItem, serverUrl: String): String {
    val raw = item.url ?: "/api/media/${item.fileId}"
    return if (raw.startsWith("http://") || raw.startsWith("https://")) {
        raw
    } else {
        "${serverUrl.trimEnd('/')}/${raw.trimStart('/')}"
    }
}

@Composable
fun PhoneTopHeader(
    isLiveConnected: Boolean,
    latencyMs: Long?,
    targetChannel: String,
    onSyncClick: () -> Unit
) {
    Surface(
        color = SurfaceDarkCharcoal,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(listOf(AccentPurple, Color(0xFF4C1D95)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SOLIXX",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "BOT",
                            color = AccentPurpleLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Mobile Moderation",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Connection indicator & sync
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLiveConnected) AccentEmeraldContainer else AccentRoseContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isLiveConnected) AccentEmerald else AccentRose)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isLiveConnected) (latencyMs?.let { "${it}ms" } ?: "Online") else "Offline",
                            color = if (isLiveConnected) AccentEmerald else AccentRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Refresh button
                IconButton(
                    onClick = onSyncClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yenile",
                        tint = AccentPurpleLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneBottomNavBar(
    currentTab: PhoneTab,
    pendingCount: Int,
    onTabSelected: (PhoneTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDarkCharcoal,
        contentColor = TextSecondary,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.fillMaxWidth()
    ) {
        val items = listOf(
            Triple(PhoneTab.QUEUE, "Onay Sırası", Icons.Default.HourglassTop),
            Triple(PhoneTab.HISTORY, "Duyurular", Icons.Default.Public),
            Triple(PhoneTab.FRIENDS, "Arkadaşlar", Icons.Default.People),
            Triple(PhoneTab.SETTINGS, "Ayarlar", Icons.Default.Settings)
        )

        items.forEach { (tab, label, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == PhoneTab.QUEUE && pendingCount > 0) {
                        BadgedBox(badge = {
                            Badge(
                                containerColor = AccentAmber,
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = if (pendingCount > 99) "99+" else pendingCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }) {
                            Icon(imageVector = icon, contentDescription = label)
                        }
                    } else {
                        Icon(imageVector = icon, contentDescription = label)
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentPurpleLight,
                    selectedTextColor = AccentPurpleLight,
                    indicatorColor = AccentPurpleContainer,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

@Composable
fun PhoneAnnouncementCard(
    announcement: Announcement,
    serverUrl: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Author Avatar & Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentPurpleContainer)
                            .border(1.dp, AccentPurple.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = announcement.senderDisplayName.take(1).uppercase().ifEmpty { "U" },
                            color = AccentPurpleLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = announcement.senderDisplayName,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (!announcement.senderUsername.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "@${announcement.senderUsername}",
                                    color = TelegramBlue,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "TG ID: ${announcement.senderTelegramId} • ${announcement.createdAt.take(16)}",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Status pill
                val (bgCol, txtCol, statusText) = when (announcement.status) {
                    "APPROVED" -> Triple(AccentEmeraldContainer, AccentEmerald, "Yayında")
                    "REJECTED" -> Triple(AccentRoseContainer, AccentRose, "Reddedildi")
                    else -> Triple(Color(0xFF332005), AccentAmber, "Bekliyor")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgCol)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(text = statusText, color = txtCol, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Photo or Album Visual Preview
            if (announcement.contentType == "PHOTO" && announcement.mediaItems.isNotEmpty()) {
                val photoUrl = resolvePhoneMediaUrl(announcement.mediaItems.first(), serverUrl)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E1035))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = photoUrl,
                        contentDescription = "Fotoğraf",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                        loading = {
                            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AccentPurpleLight, modifier = Modifier.size(24.dp))
                            }
                        },
                        error = {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = AccentPurpleLight, modifier = Modifier.size(36.dp))
                        }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else if (announcement.contentType == "ALBUM" && announcement.mediaItems.isNotEmpty()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📸 Albüm (${announcement.mediaItems.size} Görsel)",
                            color = AccentPurpleLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Yana kaydırın",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(announcement.mediaItems) { idx, item ->
                            val itemUrl = resolvePhoneMediaUrl(item, serverUrl)
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF281C45))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = itemUrl,
                                    contentDescription = "Albüm ${idx + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize(),
                                    loading = {
                                        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = AccentPurpleLight, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    error = {
                                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = AccentPurpleLight)
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "${idx + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Announcement Text
            if (!announcement.textContent.isNullOrBlank()) {
                Text(
                    text = announcement.textContent,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Rejection reason banner
            if (announcement.status == "REJECTED" && !announcement.rejectionReason.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentRoseContainer)
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Ret: ${announcement.rejectionReason}", color = AccentRose, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row (One-Handed Mobile Optimized)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (announcement.status == "PENDING") {
                    // Big Green Approve Button
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Onayla", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Red Reject Button
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reddet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Edit button
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleLight)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Düzenle", modifier = Modifier.size(18.dp))
                    }
                }

                // Preview Button (always accessible)
                OutlinedButton(
                    onClick = onPreview,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(42.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TelegramBlue)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = "Önizle", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
