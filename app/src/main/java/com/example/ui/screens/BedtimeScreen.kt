package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AlarmSoundManager
import com.example.data.PreferencesManager
import com.example.ui.components.WheelTimePicker
import com.example.ui.theme.*
import com.example.util.TimeFormatter

@Composable
fun BedtimeScreen(
    prefs: PreferencesManager,
    is24Hour: Boolean,
    soundManager: AlarmSoundManager,
    modifier: Modifier = Modifier
) {
    var bedtimeHour by remember { mutableIntStateOf(prefs.bedtimeHour) }
    var bedtimeMinute by remember { mutableIntStateOf(prefs.bedtimeMinute) }
    var wakeUpHour by remember { mutableIntStateOf(prefs.wakeUpHour) }
    var wakeUpMinute by remember { mutableIntStateOf(prefs.wakeUpMinute) }
    var reminderEnabled by remember { mutableStateOf(prefs.isBedtimeReminderEnabled) }

    var isPlayingRelaxSound by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.stop()
        }
    }

    val (sleepHours, cycles) = remember(bedtimeHour, bedtimeMinute, wakeUpHour, wakeUpMinute) {
        TimeFormatter.calculateSleepDuration(bedtimeHour, bedtimeMinute, wakeUpHour, wakeUpMinute)
    }

    val (bedtimeText, bedAmPm) = remember(bedtimeHour, bedtimeMinute, is24Hour) {
        TimeFormatter.formatTime(bedtimeHour, bedtimeMinute, is24Hour)
    }
    val (wakeText, wakeAmPm) = remember(wakeUpHour, wakeUpMinute, is24Hour) {
        TimeFormatter.formatTime(wakeUpHour, wakeUpMinute, is24Hour)
    }

    var editingMode by remember { mutableStateOf<String?>(null) } // "BED" or "WAKE" or null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("bedtime_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Column {
            Text(
                text = "وضع النوم والاسترخاء",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "جدول نوم صحي وحساب دورات النوم العميق",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
        }

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PrimaryPurpleDark.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = PrimaryPurpleLight,
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", sleepHours)} ساعة نوم متوقعة",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = "يعادل $cycles دورات نوم كاملة (90 دقيقة لكل دورة) 🌙",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AccentOrangeLight,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bedtime and Wake Up times side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Bedtime Card
                        Surface(
                            onClick = { editingMode = if (editingMode == "BED") null else "BED" },
                            shape = RoundedCornerShape(20.dp),
                            color = if (editingMode == "BED") PrimaryPurple.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = if (editingMode == "BED") androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple) else null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bedtime_picker_trigger")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NightlightRound,
                                        contentDescription = null,
                                        tint = PrimaryPurpleLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("وقت النوم", style = MaterialTheme.typography.labelMedium, color = TextSecondaryDark)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = bedtimeText,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    if (bedAmPm.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(bedAmPm, style = MaterialTheme.typography.labelSmall, color = AccentOrange)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Wake Up Card
                        Surface(
                            onClick = { editingMode = if (editingMode == "WAKE") null else "WAKE" },
                            shape = RoundedCornerShape(20.dp),
                            color = if (editingMode == "WAKE") AccentOrange.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = if (editingMode == "WAKE") androidx.compose.foundation.BorderStroke(1.5.dp, AccentOrange) else null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wakeup_picker_trigger")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("الاستيقاظ", style = MaterialTheme.typography.labelMedium, color = TextSecondaryDark)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = wakeText,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    if (wakeAmPm.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(wakeAmPm, style = MaterialTheme.typography.labelSmall, color = AccentOrange)
                                    }
                                }
                            }
                        }
                    }

                    // Expandable Wheel Picker when editing either Bedtime or Wake Up
                    if (editingMode != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (editingMode == "BED") "تحديد وقت الخلود إلى النوم" else "تحديد وقت الاستيقاظ الصباحي",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        WheelTimePicker(
                            initialHour = if (editingMode == "BED") bedtimeHour else wakeUpHour,
                            initialMinute = if (editingMode == "BED") bedtimeMinute else wakeUpMinute,
                            is24Hour = is24Hour,
                            onTimeChanged = { h, m ->
                                if (editingMode == "BED") {
                                    bedtimeHour = h
                                    bedtimeMinute = m
                                    prefs.bedtimeHour = h
                                    prefs.bedtimeMinute = m
                                } else {
                                    wakeUpHour = h
                                    wakeUpMinute = m
                                    prefs.wakeUpHour = h
                                    prefs.wakeUpMinute = m
                                }
                            }
                        )
                    }
                }
            }
        }

        // Calming Sleep Sounds
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "أصوات الاسترخاء قبل النوم 🌊",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "أمواج البحر الهادئة لمساعدتك على الاستغراق في النوم",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (isPlayingRelaxSound) {
                            soundManager.stop()
                            isPlayingRelaxSound = false
                        } else {
                            isPlayingRelaxSound = true
                            soundManager.playAlarm(
                                soundId = "waves",
                                isVibrate = false,
                                isGradualVolume = false,
                                isPreview = false
                            )
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isPlayingRelaxSound) AccentOrange else PrimaryPurple
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("play_relax_sound_button")
                ) {
                    Icon(
                        imageVector = if (isPlayingRelaxSound) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل صوت الاسترخاء",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Bedtime Reminder Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تذكير بوقت النوم",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "إشعار قبل 30 دقيقة من موعد النوم للاستعداد",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }

                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = {
                        reminderEnabled = it
                        prefs.isBedtimeReminderEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryPurple
                    )
                )
            }
        }

        // Tips for healthy sleep
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = AccentOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إرشادات نوم عميق وصحي",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• حافظ على جدول نوم واستيقاظ ثابت حتى في عطلة نهاية الأسبوع.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• تجنب الشاشات والهواتف الذكية قبل 30-45 دقيقة من النوم.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• 5 دورات نوم (7.5 ساعات) هي المعدل المثالي للاستيقاظ بنشاط وطاقة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryDark
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
