package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.logger.AppErrorLogger
import com.example.data.model.SystemLog
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
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ErrorLogsScreen(
    logs: List<SystemLog>,
    onBackClick: () -> Unit,
    onClearLogs: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedLevel by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedLogIds by remember { mutableStateOf(setOf<Long>()) }

    // Counts for quick metrics
    val totalCount = logs.size
    val networkErrorsCount = logs.count { it.category == "NETWORK" }
    val apiErrorsCount = logs.count { it.category == "API" }
    val jsonErrorsCount = logs.count { it.category == "JSON_PARSE" }
    val totalErrorsCount = logs.count { it.level == "ERROR" }

    // Filtered logs
    val filteredLogs = logs.filter { log ->
        val matchCategory = when (selectedCategory) {
            "ALL" -> true
            "NETWORK" -> log.category == "NETWORK"
            "API" -> log.category == "API"
            "JSON_PARSE" -> log.category == "JSON_PARSE"
            "SYSTEM" -> log.category !in listOf("NETWORK", "API", "JSON_PARSE")
            else -> log.category == selectedCategory
        }
        val matchLevel = when (selectedLevel) {
            "ALL" -> true
            else -> log.level == selectedLevel
        }
        val matchSearch = if (searchQuery.isBlank()) true
        else {
            log.message.contains(searchQuery, ignoreCase = true) ||
            log.category.contains(searchQuery, ignoreCase = true) ||
            (log.details?.contains(searchQuery, ignoreCase = true) == true)
        }
        matchCategory && matchLevel && matchSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNearBlack)
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Hata Günlüğü & Tanılama",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "API, Ağ ve JSON Çözümleme Hata İzleyicisi",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val report = buildString {
                            append("=== SOLIXXBOT HATA VE TANILAMA GÜNLÜĞÜ ===\n")
                            append("Toplam Kayıt: $totalCount (Hatalar: $totalErrorsCount, Ağ: $networkErrorsCount, API: $apiErrorsCount, JSON: $jsonErrorsCount)\n\n")
                            logs.forEach { l ->
                                append("[${l.timestamp}] [${l.level}] [${l.category}] ${l.message}\n")
                                if (!l.details.isNullOrBlank()) {
                                    append("Detaylar:\n${l.details}\n")
                                }
                                append("----------------------------------------\n")
                            }
                        }
                        clipboardManager.setText(AnnotatedString(report))
                        onShowToast("📋 Tüm tanılama logları panoya kopyalandı.")
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = TelegramBlue)
                }

                IconButton(onClick = onClearLogs) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Temizle", tint = AccentRose)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Metric Cards Grid (Total, Network, API, JSON)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "Toplam Hata",
                        count = totalErrorsCount,
                        color = AccentRose,
                        containerColor = AccentRoseContainer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Ağ Kopması",
                        count = networkErrorsCount,
                        color = AccentAmber,
                        containerColor = AccentAmberContainer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "API (HTTP)",
                        count = apiErrorsCount,
                        color = AccentPurpleLight,
                        containerColor = AccentPurpleContainer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "JSON Hatası",
                        count = jsonErrorsCount,
                        color = TelegramBlue,
                        containerColor = TelegramBlue.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Test Simulation Buttons (Quickly verify monitoring works)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Hata Simülatörü:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("(İzleme sistemini doğrulamak için)", color = TextMuted, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    AppErrorLogger.simulateTestError("NETWORK")
                                    onShowToast("Ağ bağlantı kopma simülasyonu günlüğe kaydedildi.")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentAmber),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Ağ Hatası", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    AppErrorLogger.simulateTestError("API")
                                    onShowToast("HTTP 401 API hata simülasyonu günlüğe kaydedildi.")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ API 401", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    AppErrorLogger.simulateTestError("JSON_PARSE")
                                    onShowToast("JSON çözümleme hatası simülasyonu günlüğe kaydedildi.")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TelegramBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ JSON Hatası", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Category Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kategoriye Göre Filtrele:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "Tümü",
                            "NETWORK" to "Ağ",
                            "API" to "API (HTTP)",
                            "JSON_PARSE" to "JSON",
                            "SYSTEM" to "Sistem"
                        ).forEach { (code, label) ->
                            val isSelected = selectedCategory == code
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentPurple else SurfaceVariantDark)
                                    .border(1.dp, if (isSelected) AccentPurpleLight else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = code }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BackgroundNearBlack else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 4. Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hata mesajı, endpoint veya yığın izinde ara...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("error_logs_search_input"),
                    shape = RoundedCornerShape(10.dp),
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

            // 5. Logs List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kayıt Listesi (${filteredLogs.size})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (filteredLogs.isNotEmpty()) {
                        Text(
                            text = "Detaylar için karta dokunun",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 6. Log Entries or Empty State
            if (filteredLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AccentEmeraldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Kayıtlı Hata Bulunmuyor",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ağ bağlantısı, API istekleri ve JSON çözümlemeleri sağlıklı çalışıyor.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    val isExpanded = expandedLogIds.contains(log.id)
                    val categoryColor = when (log.category) {
                        "NETWORK" -> AccentAmber
                        "API" -> AccentRose
                        "JSON_PARSE" -> TelegramBlue
                        else -> AccentPurpleLight
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                expandedLogIds = if (isExpanded) {
                                    expandedLogIds - log.id
                                } else {
                                    expandedLogIds + log.id
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(categoryColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = log.category,
                                            color = categoryColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.level,
                                        color = if (log.level == "ERROR") AccentRose else AccentEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = log.timestamp, color = TextMuted, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = log.message,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF090A0F))
                                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Teknik Tanılama & Detay:",
                                                    color = TextMuted,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(log.details ?: log.message))
                                                        onShowToast("Hata ayrıntısı kopyalandı.")
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = TelegramBlue, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = log.details ?: "Ek yığın izi bulunmuyor.",
                                                color = Color(0xFFFCA5A5),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    count: Int,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor.copy(alpha = 0.25f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$count", color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(text = title, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
