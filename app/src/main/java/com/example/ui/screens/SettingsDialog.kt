package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.PreferencesManager
import com.example.ui.theme.*

@Composable
fun SettingsDialog(
    prefs: PreferencesManager,
    onSettingsChanged: () -> Unit,
    onDismiss: () -> Unit
) {
    var is24Hour by remember { mutableStateOf(prefs.is24HourFormat) }
    var defaultSnooze by remember { mutableIntStateOf(prefs.defaultSnoozeMinutes) }
    var weekStartSaturday by remember { mutableStateOf(prefs.weekStartSaturday) }
    var themeMode by remember { mutableStateOf(prefs.themeMode) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp))
                .testTag("settings_dialog"),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الإعدادات العامة",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Time Format (12h vs 24h)
                SettingCard(title = "نظام عرض الوقت", subtitle = "اختر بين نظام 12 ساعة أو 24 ساعة") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = {
                                is24Hour = false
                                prefs.is24HourFormat = false
                                onSettingsChanged()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (!is24Hour) PrimaryPurple else DarkSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text("12 ساعة (ص/م)", color = if (!is24Hour) Color.White else TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Surface(
                            onClick = {
                                is24Hour = true
                                prefs.is24HourFormat = true
                                onSettingsChanged()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (is24Hour) PrimaryPurple else DarkSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text("24 ساعة (عسكري)", color = if (is24Hour) Color.White else TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Default Snooze Duration
                SettingCard(title = "مدة الغفوة الافتراضية", subtitle = "المدة الزمنية التلقائية للمنبهات الجديدة") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(5, 10, 15).forEach { mins ->
                            val isSelected = defaultSnooze == mins
                            Surface(
                                onClick = {
                                    defaultSnooze = mins
                                    prefs.defaultSnoozeMinutes = mins
                                    onSettingsChanged()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AccentOrange else DarkSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text("$mins دقائق", color = if (isSelected) Color.White else TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Week Start Day
                SettingCard(title = "بداية الأسبوع", subtitle = "اليوم الأول في تقويم الأسبوع") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = {
                                weekStartSaturday = true
                                prefs.weekStartSaturday = true
                                onSettingsChanged()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (weekStartSaturday) PrimaryPurple else DarkSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text("السبت", color = if (weekStartSaturday) Color.White else TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Surface(
                            onClick = {
                                weekStartSaturday = false
                                prefs.weekStartSaturday = false
                                onSettingsChanged()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (!weekStartSaturday) PrimaryPurple else DarkSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text("الأحد", color = if (!weekStartSaturday) Color.White else TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App Theme
                SettingCard(title = "المظهر والثيم", subtitle = "اختيار النمط الداكن أو الفاتح") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("DARK" to "داكن 🌙", "LIGHT" to "فاتح ☀️", "SYSTEM" to "النظام ⚙️").forEach { (mode, label) ->
                            val isSelected = themeMode == mode
                            Surface(
                                onClick = {
                                    themeMode = mode
                                    prefs.themeMode = mode
                                    onSettingsChanged()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryPurple else DarkSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(label, color = if (isSelected) Color.White else TextSecondaryDark, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("تم وحفظ الإعدادات", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextMutedDark)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
