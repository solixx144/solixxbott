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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.phone.data.model.Announcement
import com.example.phone.data.model.MediaItem
import com.example.phone.ui.theme.AccentEmerald
import com.example.phone.ui.theme.AccentEmeraldContainer
import com.example.phone.ui.theme.AccentPurple
import com.example.phone.ui.theme.AccentPurpleLight
import com.example.phone.ui.theme.AccentRose
import com.example.phone.ui.theme.AccentRoseContainer
import com.example.phone.ui.theme.BorderSubtle
import com.example.phone.ui.theme.SurfaceDarkCharcoal
import com.example.phone.ui.theme.SurfaceVariantDark
import com.example.phone.ui.theme.TelegramBlue
import com.example.phone.ui.theme.TextMuted
import com.example.phone.ui.theme.TextPrimary
import com.example.phone.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhoneRejectDialog(
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
                    fontSize = 17.sp
                )
                Text(
                    text = "Gönderen: ${announcement.senderDisplayName} (No: #${announcement.id})",
                    color = TextMuted,
                    fontSize = 11.sp
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
                    text = "Hızlı Gerekçe:",
                    color = TextSecondary,
                    fontSize = 11.sp,
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
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Kullanıcıya Gönderilecek Açıklama *",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    placeholder = { Text("Gerekçeyi yazın...", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentRose,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Yönetici Notu (Opsiyonel):",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Dahili yönetici notu", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
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
                        onConfirm(reasonText.trim(), notesText.trim().ifEmpty { null })
                    }
                },
                enabled = reasonText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
            ) {
                Text("Reddet ve Bildir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Vazgeç", fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun PhoneEditDialog(
    announcement: Announcement,
    serverUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (newText: String, notes: String?, mediaItems: List<MediaItem>?) -> Unit
) {
    var editedText by remember { mutableStateOf(announcement.textContent ?: "") }
    var notesText by remember { mutableStateOf(announcement.adminNotes ?: "") }
    var mediaList by remember { mutableStateOf(announcement.mediaItems) }

    var showAddModal by remember { mutableStateOf(false) }
    var newUrl by remember { mutableStateOf("") }
    var newCaption by remember { mutableStateOf("") }

    var replaceIdx by remember { mutableIntStateOf(-1) }
    var replaceUrlVal by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkCharcoal,
        title = {
            Column {
                Text("Duyuru Düzenle (No: #${announcement.id})", color = AccentPurpleLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(announcement.senderDisplayName, color = TextMuted, fontSize = 11.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Media List Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fotoğraflar (${mediaList.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedButton(
                        onClick = { showAddModal = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleLight),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fotoğraf Ekle", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (mediaList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariantDark)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Medya yok. Metin duyurusu.", color = TextMuted, fontSize = 11.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        mediaList.forEachIndexed { idx, item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF281C45)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val resolved = resolvePhoneMediaUrl(item, serverUrl)
                                        SubcomposeAsyncImage(
                                            model = resolved,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize(),
                                            loading = {
                                                Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                                    CircularProgressIndicator(color = AccentPurpleLight, modifier = Modifier.size(14.dp))
                                                }
                                            },
                                            error = {
                                                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = AccentPurpleLight, modifier = Modifier.size(20.dp))
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("#${idx + 1} (${item.type.uppercase()})", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(item.fileId.take(14) + "...", color = TextMuted, fontSize = 9.sp)
                                    }

                                    // Action buttons: Reorder, Replace, Delete
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (idx > 0) {
                                                    val nl = mediaList.toMutableList()
                                                    val t = nl[idx]
                                                    nl[idx] = nl[idx - 1]
                                                    nl[idx - 1] = t
                                                    mediaList = nl
                                                }
                                            },
                                            enabled = idx > 0,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı", tint = if (idx > 0) AccentPurpleLight else TextMuted.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                if (idx < mediaList.size - 1) {
                                                    val nl = mediaList.toMutableList()
                                                    val t = nl[idx]
                                                    nl[idx] = nl[idx + 1]
                                                    nl[idx + 1] = t
                                                    mediaList = nl
                                                }
                                            },
                                            enabled = idx < mediaList.size - 1,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Aşağı", tint = if (idx < mediaList.size - 1) AccentPurpleLight else TextMuted.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                replaceIdx = idx
                                                replaceUrlVal = item.fileId
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Değiştir", tint = TelegramBlue, modifier = Modifier.size(14.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                val nl = mediaList.toMutableList()
                                                nl.removeAt(idx)
                                                mediaList = nl
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = AccentRose, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Metin / Açıklama (Caption):", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Yönetici Notu:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Opsiyonel not...", color = TextMuted, fontSize = 11.sp) },
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
                onClick = { onConfirm(editedText, notesText.ifEmpty { null }, mediaList) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Vazgeç", fontSize = 12.sp)
            }
        }
    )

    // Add Photo Modal
    if (showAddModal) {
        AlertDialog(
            onDismissRequest = { showAddModal = false },
            containerColor = SurfaceDarkCharcoal,
            title = { Text("Fotoğraf / Görsel Ekle", color = AccentPurpleLight, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column {
                    Text("Görsel URL veya Telegram File ID:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        placeholder = { Text("https://... veya file_id", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("İsteğe Bağlı Açıklama:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newCaption,
                        onValueChange = { newCaption = it },
                        placeholder = { Text("Caption...", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUrl.isNotBlank()) {
                            val item = MediaItem(
                                fileId = newUrl.trim(),
                                type = "photo",
                                caption = newCaption.trim().ifEmpty { null },
                                url = if (newUrl.startsWith("http")) newUrl.trim() else "/api/media/${newUrl.trim()}"
                            )
                            mediaList = mediaList + item
                            newUrl = ""
                            newCaption = ""
                            showAddModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddModal = false }) { Text("İptal") }
            }
        )
    }

    // Replace Photo Modal
    if (replaceIdx >= 0) {
        AlertDialog(
            onDismissRequest = { replaceIdx = -1 },
            containerColor = SurfaceDarkCharcoal,
            title = { Text("Görseli Değiştir (#${replaceIdx + 1})", color = TelegramBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column {
                    Text("Yeni URL veya File ID:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = replaceUrlVal,
                        onValueChange = { replaceUrlVal = it },
                        placeholder = { Text("https://... veya file_id", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replaceUrlVal.isNotBlank() && replaceIdx in mediaList.indices) {
                            val curr = mediaList[replaceIdx]
                            val upd = curr.copy(
                                fileId = replaceUrlVal.trim(),
                                url = if (replaceUrlVal.startsWith("http")) replaceUrlVal.trim() else "/api/media/${replaceUrlVal.trim()}"
                            )
                            val nl = mediaList.toMutableList()
                            nl[replaceIdx] = upd
                            mediaList = nl
                            replaceIdx = -1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Değiştir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { replaceIdx = -1 }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun PhoneAddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: (telegramId: Long, name: String?, username: String?, notes: String?) -> Unit
) {
    var tgIdText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var usernameText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDarkCharcoal,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = AccentPurpleLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yetkili Arkadaş Ekle", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Telegram Numeric User ID *", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tgIdText,
                    onValueChange = { tgIdText = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("Örn: 123456789", color = TextMuted, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Ad / Görünen İsim:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    placeholder = { Text("Örn: Ahmet Yılmaz", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Telegram Kullanıcı Adı (İsteğe bağlı):", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = usernameText,
                    onValueChange = { usernameText = it },
                    placeholder = { Text("@ olmadan yazın", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Yönetici Notu:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Dahili not...", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idLong = tgIdText.toLongOrNull()
                    if (idLong != null && idLong > 0) {
                        onConfirm(idLong, nameText.ifEmpty { null }, usernameText.ifEmpty { null }, notesText.ifEmpty { null })
                    }
                },
                enabled = tgIdText.toLongOrNull() != null && tgIdText.toLongOrNull()!! > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Yetkilendir", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Vazgeç", fontSize = 12.sp) }
        }
    )
}

@Composable
fun PhonePreviewDialog(
    announcement: Announcement,
    serverUrl: String,
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
                    Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Telegram Kanal Önizlemesi", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, TelegramBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF17212B))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TelegramBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("S", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("SolixxTR Resmi Kanal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(12.dp))
                                }
                                Text("@SolixxTR • Şimdi", color = Color(0xFF708499), fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (announcement.mediaItems.isNotEmpty()) {
                            val first = announcement.mediaItems.first()
                            val resolved = resolvePhoneMediaUrl(first, serverUrl)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF281C45)),
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = resolved,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = announcement.textContent ?: "(Metin bulunmuyor)",
                            color = Color(0xFFE4ECF2),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (announcement.status == "PENDING") {
                Button(
                    onClick = onApproveDirectly,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Doğrudan Onayla", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Kapat", fontSize = 12.sp) }
        }
    )
}
