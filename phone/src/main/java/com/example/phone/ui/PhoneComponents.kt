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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.phone.data.model.Announcement
import com.example.phone.data.model.MediaItem
import com.example.phone.ui.theme.AccentAmber
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

fun resolvePhoneMediaUrl(item: MediaItem, serverUrl: String): String {
    val raw = item.url ?: "/api/media/${item.fileId}"
    if (raw.startsWith("http://") || raw.startsWith("https://")) {
        return raw
    }
    val base = if (serverUrl.isBlank()) {
        "http://127.0.0.1:8000"
    } else {
        val trimmed = serverUrl.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "http://$trimmed"
        } else {
            trimmed
        }
    }
    return "${base.trimEnd('/')}/${raw.trimStart('/')}"
}

@Composable
fun PhoneHeader(
    isLiveConnected: Boolean,
    latencyMs: Long?,
    isRefreshing: Boolean,
    onSyncClick: () -> Unit
) {
    Surface(
        color = SurfaceDarkCharcoal,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(AccentPurple, Color(0xFF4C1D95)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
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
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "BOT",
                            color = AccentPurpleLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Duyuru Onay Paneli",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Connection Status & Refresh
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Connection Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLiveConnected) AccentEmeraldContainer else AccentRoseContainer)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
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
                            text = if (isLiveConnected) (latencyMs?.let { "${it}ms" } ?: "Canlı") else "Bağlantı Yok",
                            color = if (isLiveConnected) AccentEmerald else AccentRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Refresh Button
                IconButton(
                    onClick = onSyncClick,
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantDark)
                        .testTag("phone_refresh_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = AccentPurpleLight,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = AccentPurpleLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingAnnouncementCard(
    announcement: Announcement,
    serverUrl: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .testTag("pending_card_${announcement.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author Avatar & Name + ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentPurpleContainer)
                            .border(1.dp, AccentPurple.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = announcement.senderDisplayName.take(1).uppercase().ifEmpty { "U" },
                            color = AccentPurpleLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = announcement.senderDisplayName,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (!announcement.senderUsername.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "@${announcement.senderUsername}",
                                    color = TelegramBlue,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = "TG ID: ${announcement.senderTelegramId} • ${announcement.createdAt.take(16)}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // ID Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "#${announcement.id}",
                        color = AccentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Single Photo Display
            if (announcement.contentType == "PHOTO" && announcement.mediaItems.isNotEmpty()) {
                val photoUrl = resolvePhoneMediaUrl(announcement.mediaItems.first(), serverUrl)
                val context = androidx.compose.ui.platform.LocalContext.current
                val imageLoader = remember {
                    com.example.phone.data.remote.ImageLoaderHelper.getAuthenticatedImageLoader(context, "")
                }
                val request = remember(photoUrl) {
                    com.example.phone.data.remote.ImageLoaderHelper.buildImageRequest(context, photoUrl, "")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF281C45), Color(0xFF130E24))))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = request,
                        imageLoader = imageLoader,
                        contentDescription = "Duyuru Fotoğrafı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                        loading = {
                            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = AccentPurpleLight, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Fotoğraf yükleniyor...", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.BrokenImage,
                                        contentDescription = "Fotoğraf yüklenemedi",
                                        tint = AccentRose,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Fotoğraf yüklenemedi",
                                        color = AccentRose,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Sunucu bağlantısını veya Telegram medyasını kontrol edin",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else if (announcement.contentType == "ALBUM" && announcement.mediaItems.isNotEmpty()) {
                // Album Display (Horizontal gallery)
                val context = androidx.compose.ui.platform.LocalContext.current
                val imageLoader = remember {
                    com.example.phone.data.remote.ImageLoaderHelper.getAuthenticatedImageLoader(context, "")
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = AccentPurpleLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Albüm (${announcement.mediaItems.size} Görsel)",
                            color = AccentPurpleLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(announcement.mediaItems) { idx, item ->
                            val itemUrl = resolvePhoneMediaUrl(item, serverUrl)
                            val request = remember(itemUrl) {
                                com.example.phone.data.remote.ImageLoaderHelper.buildImageRequest(context, itemUrl, "")
                            }

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF281C45))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = request,
                                    imageLoader = imageLoader,
                                    contentDescription = "Görsel ${idx + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize(),
                                    loading = {
                                        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = AccentPurpleLight, modifier = Modifier.size(18.dp))
                                        }
                                    },
                                    error = {
                                        Icon(imageVector = Icons.Default.BrokenImage, contentDescription = null, tint = AccentRose)
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${idx + 1}/${announcement.mediaItems.size}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Announcement Text Content
            if (!announcement.textContent.isNullOrBlank()) {
                Text(
                    text = announcement.textContent,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Prominent Approve and Reject Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Yeşil Onayla Butonu (Geniş ve Belirgin)
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("phone_approve_btn_${announcement.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Onayla",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                // Kırmızı Reddet Butonu
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("phone_reject_btn_${announcement.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reddet",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhoneRejectDialog(
    announcement: Announcement,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reasonText by remember { mutableStateOf("") }

    val quickReasons = listOf(
        "Kanal yayın kurallarına aykırı.",
        "Düşük görüntü kalitesi veya bozuk medya.",
        "Eksik / yanıltıcı bilgi.",
        "Mükerrer veya daha önce paylaşılmış içerik.",
        "Uygunsuz dil kullanımı."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkCharcoal,
        title = {
            Column {
                Text(
                    text = "Duyuruyu Reddet",
                    color = AccentRose,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Duyuru #${announcement.id} • ${announcement.senderDisplayName}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Hızlı Gerekçe Seçin:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickReasons.forEach { r ->
                        val isSelected = reasonText == r
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentRoseContainer else SurfaceVariantDark)
                                .border(1.dp, if (isSelected) AccentRose else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { reasonText = r }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = r,
                                color = if (isSelected) AccentRose else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Kullanıcıya İletilecek Ret Nedeni *",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    placeholder = { Text("Ret nedenini yazın...", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("reject_reason_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentRose,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reasonText.isNotBlank()) {
                        onConfirm(reasonText.trim())
                    }
                },
                enabled = reasonText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                modifier = Modifier.testTag("confirm_reject_button")
            ) {
                Text("Reddet ve Bildir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Vazgeç", fontSize = 13.sp)
            }
        }
    )
}
