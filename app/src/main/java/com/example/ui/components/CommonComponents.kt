package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.data.model.MediaItem
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberContainer
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentEmeraldContainer
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleContainer
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentRoseContainer
import com.example.ui.theme.BackgroundNearBlack
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDarkCharcoal
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SolixxTopHeader(
    botOnline: Boolean,
    targetChannel: String = "@SolixxTR",
    isLocalMode: Boolean = true,
    onTestClick: () -> Unit = {},
    onSyncClick: () -> Unit = {}
) {
    Surface(
        color = SurfaceDarkCharcoal,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_header")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentPurple, Color(0xFF4C1D95))
                            )
                        )
                        .border(1.dp, AccentPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SOLIXX",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "BOT",
                            color = AccentPurpleLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Moderation System",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Status Badges (Channel, Bot Online, & Sync)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Target Channel Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TelegramBlue.copy(alpha = 0.15f))
                        .border(1.dp, TelegramBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Channel",
                            tint = TelegramBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = targetChannel,
                            color = TelegramBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bot Live Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (botOnline) AccentEmeraldContainer else AccentRoseContainer)
                        .border(
                            1.dp,
                            if (botOnline) AccentEmerald.copy(alpha = 0.4f) else AccentRose.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onTestClick() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (botOnline) AccentEmerald else AccentRose)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (botOnline) "BOT ONLINE" else "OFFLINE",
                            color = if (botOnline) AccentEmerald else AccentRose,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Manual Remote Sync Pill (shown when live connected or not in local mode)
                if (!isLocalMode) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AccentPurpleContainer)
                            .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { onSyncClick() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = AccentPurpleLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "YENİLE",
                                color = AccentPurpleLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, borderColor, icon) = when (status) {
        "APPROVED" -> Quad(AccentEmeraldContainer, AccentEmerald, AccentEmerald.copy(alpha = 0.5f), Icons.Default.CheckCircle)
        "REJECTED" -> Quad(AccentRoseContainer, AccentRose, AccentRose.copy(alpha = 0.5f), Icons.Default.Close)
        else -> Quad(AccentAmberContainer, AccentAmber, AccentAmber.copy(alpha = 0.5f), Icons.Default.Warning)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = status,
                tint = textColor,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (status) {
                    "APPROVED" -> "YAYIMLANDI"
                    "REJECTED" -> "REDDEDİLDİ"
                    else -> "ONAY BEKLİYOR"
                },
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ContentTypeBadge(contentType: String, mediaCount: Int = 1) {
    val (label, icon) = when (contentType) {
        "PHOTO" -> Pair("FOTOĞRAF", Icons.Default.Image)
        "ALBUM" -> Pair("ALBÜM ($mediaCount)", Icons.Default.Collections)
        "VIDEO" -> Pair("VİDEO", Icons.Default.Videocam)
        "AUDIO" -> Pair("SES DOSYASI", Icons.Default.Audiotrack)
        "VOICE" -> Pair("SESLİ MESAJ", Icons.Default.Mic)
        else -> Pair("METİN", Icons.Default.TextSnippet)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AccentPurpleContainer)
            .border(1.dp, AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AccentPurpleLight,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = AccentPurpleLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MediaPreviewBox(
    contentType: String,
    mediaItems: List<MediaItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        when (contentType) {
            "PHOTO" -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF3B1D6B), Color(0xFF1E1035))))
                            .border(1.dp, AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Photo",
                            tint = AccentPurpleLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Telegram Fotoğraf Medyası",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Dosya ID: ${mediaItems.firstOrNull()?.fileId?.take(18) ?: "tg_photo"}... (Orijinal kalite korunuyor)",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            "ALBUM" -> {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "Album",
                                tint = AccentPurpleLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Medya Grubu / Albüm (${mediaItems.size} Öğe)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "Tek Paylaşım",
                            color = AccentEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val displayCount = minOf(4, mediaItems.size)
                        for (i in 0 until displayCount) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF281C45), Color(0xFF18122B))))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (i == 3 && mediaItems.size > 4) {
                                    Text(
                                        text = "+${mediaItems.size - 3}",
                                        color = AccentPurpleLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Photo $i",
                                        tint = AccentPurpleLight.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "VIDEO" -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = AccentIndigo,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Telegram Video Yayını",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Süre: ${mediaItems.firstOrNull()?.duration ?: 30} sn • Doğrudan video akışı",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            "AUDIO", "VOICE" -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AccentPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (contentType == "VOICE") Icons.Default.Mic else Icons.Default.Audiotrack,
                            contentDescription = "Audio",
                            tint = AccentPurpleLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (contentType == "VOICE") "Sesli Mesaj Kaydı" else "Ses Parçası",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        // Simulated waveform lines
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf(6, 14, 18, 10, 22, 16, 8, 12, 20, 15, 9, 14).forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(h.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(AccentPurpleLight)
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TextSnippet,
                        contentDescription = "Text",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Metin Duyurusu",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
