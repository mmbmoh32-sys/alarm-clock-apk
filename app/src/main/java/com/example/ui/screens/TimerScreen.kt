package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.AlarmSoundManager
import com.example.data.TimerPreset
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TimerScreen(
    presets: List<TimerPreset>,
    soundManager: AlarmSoundManager,
    onAddPreset: (TimerPreset) -> Unit,
    onDeletePreset: (TimerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalSeconds by remember { mutableIntStateOf(300) } // Default 5 mins
    var remainingSeconds by remember { mutableIntStateOf(300) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    var showAddPresetDialog by remember { mutableStateOf(false) }

    // Picker state for inputting time
    var pickHours by remember { mutableIntStateOf(0) }
    var pickMinutes by remember { mutableIntStateOf(5) }
    var pickSeconds by remember { mutableIntStateOf(0) }

    // Countdown loop
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
            if (remainingSeconds <= 0) {
                isRunning = false
                isFinished = true
                soundManager.playAlarm(
                    soundId = "radar",
                    isVibrate = true,
                    isGradualVolume = false,
                    isPreview = false
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.stop()
        }
    }

    val progress = remember(remainingSeconds, totalSeconds) {
        if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
    }

    val hoursLeft = remainingSeconds / 3600
    val minutesLeft = (remainingSeconds % 3600) / 60
    val secondsLeft = remainingSeconds % 60

    val formattedTime = remember(remainingSeconds) {
        if (hoursLeft > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hoursLeft, minutesLeft, secondsLeft)
        } else {
            String.format(Locale.US, "%02d:%02d", minutesLeft, secondsLeft)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "المؤقت التنازلي",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "عد تنازلي دقيق مع مؤقتات مخصصة جاهزة",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            IconButton(
                onClick = { showAddPresetDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .testTag("add_preset_button")
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = "حفظ مؤقت جاهز", tint = AccentOrange)
            }
        }

        // Circular Timer Visual
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(230.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isFinished) AccentRed else if (isRunning) AccentOrange else PrimaryPurple,
                        trackColor = DarkSurfaceVariant,
                        strokeWidth = 12.dp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (hoursLeft > 0) 40.sp else 52.sp
                            ),
                            color = Color.White
                        )

                        Text(
                            text = if (isFinished) "انتهى الوقت! 🔔" else if (isRunning) "قيد العد..." else "متوقف",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isFinished) AccentRed else if (isRunning) AccentOrange else TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    FilledIconButton(
                        onClick = {
                            isRunning = false
                            isFinished = false
                            soundManager.stop()
                            remainingSeconds = totalSeconds
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("reset_timer_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط", tint = TextSecondaryDark)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Play / Pause Button
                    Button(
                        onClick = {
                            if (isFinished) {
                                isFinished = false
                                soundManager.stop()
                                remainingSeconds = totalSeconds
                            } else {
                                isRunning = !isRunning
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AccentOrange else PrimaryPurple
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("start_pause_timer_button")
                    ) {
                        Icon(
                            imageVector = if (isFinished) Icons.Default.Stop else if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "إيقاف مؤقت" else "بدء",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // +1 Minute button
                    FilledIconButton(
                        onClick = {
                            remainingSeconds += 60
                            totalSeconds += 60
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("add_one_minute_button")
                    ) {
                        Text("+1د", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimaryDark)
                    }
                }
            }
        }

        // Manual Time Adjuster Card (when not running)
        AnimatedVisibility(visible = !isRunning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ضبط المدة يدوياً",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours
                        TimeStepper(label = "ساعة", value = pickHours, onValueChange = {
                            pickHours = it.coerceIn(0, 23)
                            totalSeconds = pickHours * 3600 + pickMinutes * 60 + pickSeconds
                            remainingSeconds = totalSeconds
                        })

                        Text(":", style = MaterialTheme.typography.headlineMedium, color = PrimaryPurple)

                        // Minutes
                        TimeStepper(label = "دقيقة", value = pickMinutes, onValueChange = {
                            pickMinutes = it.coerceIn(0, 59)
                            totalSeconds = pickHours * 3600 + pickMinutes * 60 + pickSeconds
                            remainingSeconds = totalSeconds
                        })

                        Text(":", style = MaterialTheme.typography.headlineMedium, color = PrimaryPurple)

                        // Seconds
                        TimeStepper(label = "ثانية", value = pickSeconds, onValueChange = {
                            pickSeconds = it.coerceIn(0, 59)
                            totalSeconds = pickHours * 3600 + pickMinutes * 60 + pickSeconds
                            remainingSeconds = totalSeconds
                        })
                    }
                }
            }
        }

        // Ready Presets Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مؤقتات جاهزة ومحفوظة",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "${presets.size} مؤقت",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presets, key = { it.id }) { preset ->
                    Surface(
                        onClick = {
                            isRunning = false
                            isFinished = false
                            soundManager.stop()
                            totalSeconds = preset.durationSeconds
                            remainingSeconds = preset.durationSeconds
                            pickHours = preset.durationSeconds / 3600
                            pickMinutes = (preset.durationSeconds % 3600) / 60
                            pickSeconds = preset.durationSeconds % 60
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = DarkSurfaceVariant,
                        border = if (totalSeconds == preset.durationSeconds) androidx.compose.foundation.BorderStroke(1.5.dp, AccentOrange) else null,
                        modifier = Modifier.testTag("preset_${preset.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.getFormattedDuration(),
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentOrangeLight
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Add Preset Dialog
    if (showAddPresetDialog) {
        AddPresetDialog(
            onDismiss = { showAddPresetDialog = false },
            onSave = { newPreset ->
                onAddPreset(newPreset)
                showAddPresetDialog = false
            }
        )
    }
}

@Composable
private fun TimeStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onValueChange(value + 1) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "زيادة", tint = PrimaryPurpleLight)
        }
        Text(
            text = String.format(Locale.US, "%02d", value),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
        IconButton(onClick = { onValueChange(value - 1) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "نقصان", tint = PrimaryPurpleLight)
        }
    }
}

@Composable
private fun AddPresetDialog(
    onDismiss: () -> Unit,
    onSave: (TimerPreset) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var minutes by remember { mutableIntStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "إضافة مؤقت مخصص جديد",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم المؤقت (مثال: شاي بالنعناع)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المدة بالدقائق:", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (minutes > 1) minutes-- }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = PrimaryPurple)
                        }
                        Text("$minutes دقيقة", color = AccentOrange, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { minutes++ }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryPurple)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && minutes > 0) {
                            onSave(TimerPreset(title = title, durationSeconds = minutes * 60))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("حفظ المؤقت", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
