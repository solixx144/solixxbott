package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.data.model.Friend
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentEmeraldContainer
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleContainer
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentRoseContainer
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDarkCharcoal
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RejectDialog(
    announcement: Announcement,
    onDismiss: () -> Unit,
    onConfirm: (reason: String, notes: String?) -> Unit
) {
    var reasonText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val quickReasons = listOf(
        "Kanal yayın kurallarına aykırı.",
        "Düşük görüntü kalitesi veya bozuk medya.",
        "Eksik kaynak / doğrulanamayan bilgi.",
        "Mükerrer veya daha önce yayımlanmış içerik.",
        "Telif hakkı / uygunsuz dil kullanımı."
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
                    text = "Gönderen: ${announcement.senderDisplayName} (ID: #${announcement.id})",
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
                                .padding(horizontal = 8.dp, vertical = 5.dp)
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
                    text = "Kullanıcıya Gönderilecek Açıklama *",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    placeholder = { Text("Ret gerekçesini detaylı yazın...", color = TextMuted, fontSize = 12.sp) },
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

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Yönetici Dahili Notu (İsteğe Bağlı):",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Sadece yöneticinin göreceği not...", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
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
                        onConfirm(reasonText.trim(), notesText.trim().ifEmpty { null })
                    }
                },
                enabled = reasonText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                modifier = Modifier.testTag("confirm_reject_button")
            ) {
                Text("Reddet ve Bildir", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun EditDialog(
    announcement: Announcement,
    onDismiss: () -> Unit,
    onConfirm: (newText: String, notes: String?) -> Unit
) {
    var editedText by remember { mutableStateOf(announcement.textContent ?: "") }
    var notesText by remember { mutableStateOf(announcement.adminNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkCharcoal,
        title = {
            Column {
                Text(
                    text = "Duyuruyu Düzenle (Onay Öncesi)",
                    color = AccentPurpleLight,
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
                    text = if (announcement.contentType == "TEXT") "Duyuru Metni:" else "Medya Açıklaması (Caption):",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("edit_text_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Yönetici Notu:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Düzenleme notu (isteğe bağlı)", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(editedText, notesText.ifEmpty { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                modifier = Modifier.testTag("confirm_edit_button")
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Değişiklikleri Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Vazgeç")
            }
        }
    )
}

@Composable
fun TelegramPreviewDialog(
    announcement: Announcement,
    onDismiss: () -> Unit,
    onApproveDirectly: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkCharcoal,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Telegram Kanal Önizlemesi",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Bu duyuru @SolixxTR kanalında şu şekilde görünecektir:",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Simulated Telegram Post Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, TelegramBlue.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF17212B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Channel Post Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(TelegramBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "S",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "SolixxTR Resmi Kanal",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Verified",
                                        tint = TelegramBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "@SolixxTR • Şimdi",
                                    color = Color(0xFF708499),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Media Mockup if present
                        if (announcement.contentType != "TEXT") {
                            MediaPreviewBox(
                                contentType = announcement.contentType,
                                mediaItems = announcement.mediaItems
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Formatted Text
                        Text(
                            text = announcement.textContent ?: "(Metin içeriği bulunmuyor)",
                            color = Color(0xFFE4ECF2),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFF242F3D), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Post Footer (Views + Share)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Views",
                                    tint = Color(0xFF708499),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "1.4K",
                                    color = Color(0xFF708499),
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF708499),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (announcement.status == "PENDING") {
                Button(
                    onClick = onApproveDirectly,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    modifier = Modifier.testTag("preview_approve_button")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hemen Onayla & Yayımla", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Kapat", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: (telegramId: Long, displayName: String?, username: String?, notes: String?) -> Unit
) {
    var tgIdText by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isValidId = tgIdText.trim().toLongOrNull() != null && tgIdText.trim().toLong() > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkCharcoal,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = AccentPurpleLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yeni Yetkili Arkadaş Ekle", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Yalnızca bu listede yer alan Telegram kullanıcı ID'leri bot üzerinden duyuru gönderebilir.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Telegram Kullanıcı ID (Sayısal) *", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tgIdText,
                    onValueChange = { tgIdText = it.filter { ch -> ch.isDigit() } },
                    placeholder = { Text("Telegram kullanıcı ID girin", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friend_id_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Görünen Ad / Etiket (İsteğe bağlı)", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    placeholder = { Text("Kullanıcı etiketi (boş bırakılabilir)", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friend_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Telegram Kullanıcı Adı (İsteğe bağlı)", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Kullanıcı adı (varsa, @ olmadan)", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Yetki / Rol Notu (İsteğe bağlı)", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Yetki açıklaması veya not", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
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
                    val id = tgIdText.trim().toLongOrNull() ?: return@Button
                    onConfirm(
                        id,
                        displayName.trim().ifEmpty { null },
                        username.trim().ifEmpty { null },
                        notes.trim().ifEmpty { null }
                    )
                },
                enabled = isValidId,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                modifier = Modifier.testTag("confirm_add_friend_button")
            ) {
                Text("Arkadaşı Yetkilendir", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("İptal", color = TextSecondary)
            }
        }
    )
}

@Composable
fun FriendHistoryDialog(
    friend: Friend,
    submissions: List<Announcement>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkCharcoal,
        title = {
            Column {
                Text(
                    text = "${friend.displayName} Gönderim Geçmişi",
                    color = AccentPurpleLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "TG ID: ${friend.telegramId} • Toplam: ${submissions.size} Başvuru",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            if (submissions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bu kullanıcının henüz bir gönderimi bulunmuyor.", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(submissions, key = { it.id }) { ann ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("#${ann.id} • ${ann.contentType}", color = AccentPurpleLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    StatusBadge(status = ann.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ann.textContent ?: "(Açıklama yok)",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = ann.createdAt, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)) {
                Text("Kapat")
            }
        }
    )
}
