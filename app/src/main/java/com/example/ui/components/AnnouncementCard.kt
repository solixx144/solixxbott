package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentEmeraldContainer
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleContainer
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentRoseContainer
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AnnouncementCard(
    announcement: Announcement,
    onApprove: (Announcement) -> Unit,
    onReject: (Announcement) -> Unit,
    onEdit: (Announcement) -> Unit,
    onPreview: (Announcement) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .testTag("announcement_card_${announcement.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author Avatar & Name + Status Badge
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
                            .border(1.dp, AccentPurple.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = announcement.senderDisplayName.take(1).uppercase(),
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
                            Spacer(modifier = Modifier.width(6.dp))
                            if (!announcement.senderUsername.isNullOrBlank()) {
                                Text(
                                    text = "@${announcement.senderUsername}",
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
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TG ID: ${announcement.senderTelegramId}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${announcement.createdAt}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                StatusBadge(status = announcement.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content Type & Announcement ID tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContentTypeBadge(
                    contentType = announcement.contentType,
                    mediaCount = announcement.mediaItems.size
                )

                Text(
                    text = "Duyuru #${announcement.id}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Media Preview Widget if not text-only
            if (announcement.contentType != "TEXT") {
                MediaPreviewBox(
                    contentType = announcement.contentType,
                    mediaItems = announcement.mediaItems
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Announcement Text Content
            if (!announcement.textContent.isNullOrBlank()) {
                Text(
                    text = announcement.textContent,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = if (announcement.status == "PENDING") 6 else 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Rejection reason banner if rejected
            if (announcement.status == "REJECTED" && !announcement.rejectionReason.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentRoseContainer)
                        .border(1.dp, AccentRose.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = AccentRose,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ret Gerekçesi (Kullanıcıya İletildi):",
                                color = AccentRose,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = announcement.rejectionReason,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Publication audit banner if approved
            if (announcement.status == "APPROVED") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentEmeraldContainer)
                        .border(1.dp, AccentEmerald.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "✅ @SolixxTR Kanalında Paylaşıldı",
                                color = AccentEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Tarih: ${announcement.publishedAt ?: announcement.updatedAt} • Yetkili: ${announcement.publishedBy ?: "admin"}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onPreview(announcement) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentEmerald),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Gönderi Görünümü", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Moderation Actions Toolbar (Only for PENDING queue)
            if (announcement.status == "PENDING") {
                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Preview Button
                    OutlinedButton(
                        onClick = { onPreview(announcement) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_preview_${announcement.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TelegramBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "Preview", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Önizle", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Edit Button
                    OutlinedButton(
                        onClick = { onEdit(announcement) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_edit_${announcement.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Düzenle", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reject Button
                    OutlinedButton(
                        onClick = { onReject(announcement) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_reject_${announcement.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = AccentRoseContainer.copy(alpha = 0.3f),
                            contentColor = AccentRose
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reddet", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Approve Button
                    Button(
                        onClick = { onApprove(announcement) },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("action_approve_${announcement.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Onayla & Yayımla", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
