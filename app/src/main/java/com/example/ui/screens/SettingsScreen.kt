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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
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
fun SettingsScreen(
    state: UiState,
    onSaveSettings: (url: String, apiKey: String, adminTgId: Long, targetChannel: String, isLocalMode: Boolean) -> Unit,
    onTestConnection: () -> Unit,
    onClearLogs: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var serverUrl by remember { mutableStateOf(state.serverConfig.serverUrl) }
    var apiKey by remember { mutableStateOf(state.serverConfig.apiKey) }
    var adminTgIdText by remember { mutableStateOf(if (state.serverConfig.adminTelegramId > 0) state.serverConfig.adminTelegramId.toString() else "") }
    var targetChannel by remember { mutableStateOf(state.serverConfig.targetChannel) }
    var isLocalMode by remember { mutableStateOf(state.serverConfig.isLocalMode) }
    var showTermuxGuide by remember { mutableStateOf(false) }

    // Keep fields in sync with persistent DataStore state
    LaunchedEffect(state.serverConfig) {
        serverUrl = state.serverConfig.serverUrl
        apiKey = state.serverConfig.apiKey
        adminTgIdText = if (state.serverConfig.adminTelegramId > 0) state.serverConfig.adminTelegramId.toString() else ""
        targetChannel = state.serverConfig.targetChannel
        isLocalMode = state.serverConfig.isLocalMode
    }

    fun triggerSave() {
        val tgId = adminTgIdText.trim().toLongOrNull() ?: 0L
        onSaveSettings(serverUrl.trim(), apiKey.trim(), tgId, targetChannel.trim(), isLocalMode)
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
            Text(
                text = "Sistem & Yapılandırma",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )
            Text(
                text = "Telegram botu, yönetici kimliği ve DataStore kalıcı ayarları",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // 1. Telegram & Channel Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(TelegramBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Hedef Telegram Kanalı", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(targetChannel.ifBlank { "@SolixxTR" }, color = TelegramBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentEmeraldContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("HEDEF KANAL", color = AccentEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = targetChannel,
                        onValueChange = {
                            targetChannel = it
                            triggerSave()
                        },
                        label = { Text("Kanal Kullanıcı Adı / ID", color = TextMuted, fontSize = 11.sp) },
                        placeholder = { Text("@SolixxTR", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_channel_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Bot bu kanalda yönetici olmalı ve 'Post Messages' yetkisine sahip olmalıdır.\n" +
                               "• Yetkili arkadaşların gönderdiği içerikler yalnızca sizin onayınızla bu kanala iletilir.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Administrator & Backend Connection Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(AccentPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AccentPurpleLight, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Yönetici & Sunucu Güvenliği", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Doğrulanmış Telegram ID & API Anahtarı", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        // Local / Remote Mode Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isLocalMode) "Yerel Mod" else "Canlı Sunucu",
                                color = if (isLocalMode) AccentAmber else AccentEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = !isLocalMode,
                                onCheckedChange = {
                                    isLocalMode = !it
                                    triggerSave()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AccentEmerald,
                                    checkedTrackColor = AccentEmeraldContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Yönetici Telegram Kullanıcı ID (Sayısal):", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = adminTgIdText,
                        onValueChange = {
                            adminTgIdText = it.filter { ch -> ch.isDigit() }
                            triggerSave()
                        },
                        placeholder = { Text("Telegram sayısal kullanıcı ID girin", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_tg_id_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Text(
                        text = "Yönetimsel onay ve ret işlemleri bu Telegram kimliği üzerinden yetkilendirilir.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Sunucu Adresi (URL):", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = {
                            serverUrl = it
                            triggerSave()
                        },
                        placeholder = { Text("http://10.0.2.2:8000 veya http://127.0.0.1:8000", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_url_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Yönetici API Anahtarı (X-Admin-Key):", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            triggerSave()
                        },
                        placeholder = { Text("Sunucu admin secret anahtarını girin", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_api_key_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTestConnection,
                            enabled = !state.isTestingConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_server_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isTestingConnection) "Test Ediliyor..." else "Bağlantıyı Test Et",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (state.serverConfig.latencyMs != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentEmeraldContainer)
                                    .border(1.dp, AccentEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${state.serverConfig.latencyMs} ms",
                                    color = AccentEmerald,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { triggerSave() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurpleLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_settings_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ayarları Kaydet (Kalıcı Sakla)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BackgroundNearBlack)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ Ayarlar Android DataStore ile cihazınızda kalıcı saklanır. Uygulama kapansa veya çökse bile sıfırlanmaz.",
                        color = AccentEmerald,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 3. Termux Android Deployment Guide (Expandable)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTermuxGuide = !showTermuxGuide },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Android Tablette 7/24 Çalıştırma", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Termux tek tıkla arka plan kurulum kılavuzu", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Text(
                            text = if (showTermuxGuide) "Gizle" else "Göster",
                            color = AccentPurpleLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showTermuxGuide) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Bilgisayar olmadan, kendi Android cihazınızda arka planda botu çalıştırmak için:",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D0F14))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "# Termux uygulamasını açın ve çalıştırın:\n" +
                                       "pkg update && pkg install python git sqlite -y\n" +
                                       "git clone <proje_adresi> solixxbot\n" +
                                       "cd solixxbot/backend\n" +
                                       "chmod +x run_termux.sh\n" +
                                       "./run_termux.sh",
                                color = Color(0xFF4ADE80),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Not: Backend başlatıldığında cihazda 'http://127.0.0.1:8000' adresini dinleyecektir. Buradaki adres kutusuna o adresi girerek doğrudan bağlanabilirsiniz.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 4. Database Management Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = AccentPurpleLight, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Veritabanı Yönetimi", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Veritabanı yalnızca gerçek kullanıcı ve duyuru kayıtlarını saklar. Tüm kayıtları sıfırlayarak temiz bir başlangıç yapabilirsiniz.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearAllData,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = AccentRoseContainer.copy(alpha = 0.2f),
                                contentColor = AccentRose
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verileri Sıfırla", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onClearLogs,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logları Temizle", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 5. System Audit Logs Viewer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sistem Denetim Logları",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${state.logs.size} Kayıt",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        if (state.logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz bir sistem denetim kaydı bulunmuyor.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(state.logs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val badgeColor = when (log.level) {
                                    "SUCCESS" -> AccentEmerald
                                    "WARN" -> AccentAmber
                                    "ERROR" -> AccentRose
                                    else -> AccentPurpleLight
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(badgeColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = log.level,
                                        color = badgeColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[${log.category}]",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(text = log.timestamp, color = TextMuted, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = log.message, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        if (!log.details.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = log.details, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
