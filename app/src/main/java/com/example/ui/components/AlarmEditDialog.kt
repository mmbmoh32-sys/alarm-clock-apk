package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.AlarmSoundManager
import com.example.data.Alarm
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
    alarmToEdit: Alarm?,
    is24Hour: Boolean,
    soundManager: AlarmSoundManager,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    var hour by remember { mutableIntStateOf(alarmToEdit?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(alarmToEdit?.minute ?: 0) }
    var label by remember { mutableStateOf(alarmToEdit?.label ?: "منبه جديد") }
    var selectedDays by remember {
        mutableStateOf(alarmToEdit?.getDaysList()?.toSet() ?: emptySet())
    }
    var snoozeMinutes by remember { mutableIntStateOf(alarmToEdit?.snoozeMinutes ?: 10) }
    var maxSnoozeCount by remember { mutableIntStateOf(alarmToEdit?.maxSnoozeCount ?: 3) }
    var selectedSound by remember { mutableStateOf(alarmToEdit?.soundId ?: "digital") }
    var isVibrate by remember { mutableStateOf(alarmToEdit?.isVibrate ?: true) }
    var isGradualVolume by remember { mutableStateOf(alarmToEdit?.isVolumeGradual ?: true) }
    var selectedMission by remember { mutableStateOf(alarmToEdit?.missionType ?: "NONE") }
    var missionDifficulty by remember { mutableStateOf(alarmToEdit?.missionDifficulty ?: "MEDIUM") }

    var isPreviewingSound by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.stop()
        }
    }

    val daysOfWeekMap = listOf(
        7 to "السبت",
        1 to "الأحد",
        2 to "الإثنين",
        3 to "الثلاثاء",
        4 to "الأربعاء",
        5 to "الخميس",
        6 to "الجمعة"
    )

    val soundOptions = listOf(
        "digital" to "رقمي كلاسيكي 🔔",
        "radar" to "رادار نشط ⚡",
        "dawn" to "أجراس الفجر 🕌",
        "waves" to "أمواج هادئة 🌊",
        "classic" to "جرس تقليدي ⏰",
        "gentle" to "لحن الصباح 🎵"
    )

    val missionOptions = listOf(
        "NONE" to "بدون مهمة",
        "MATH" to "معادلة رياضية 🧮",
        "SHAKE" to "هز الهاتف 📱",
        "TYPING" to "كتابة جملة ✍️"
    )

    Dialog(
        onDismissRequest = {
            soundManager.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("alarm_edit_dialog"),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with title and buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            soundManager.stop()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("cancel_alarm_button")
                    ) {
                        Text("إلغاء", color = TextSecondaryDark, style = MaterialTheme.typography.titleMedium)
                    }

                    Text(
                        text = if (alarmToEdit == null) "إضافة منبه" else "تعديل المنبه",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Button(
                        onClick = {
                            soundManager.stop()
                            val daysStr = selectedDays.sorted().joinToString(",")
                            val newAlarm = (alarmToEdit ?: Alarm(hour = hour, minute = minute)).copy(
                                hour = hour,
                                minute = minute,
                                isEnabled = true,
                                label = label,
                                daysOfWeek = daysStr,
                                snoozeMinutes = snoozeMinutes,
                                maxSnoozeCount = maxSnoozeCount,
                                soundId = selectedSound,
                                isVibrate = isVibrate,
                                isVolumeGradual = isGradualVolume,
                                missionType = selectedMission,
                                missionDifficulty = missionDifficulty
                            )
                            onSave(newAlarm)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("save_alarm_button")
                    ) {
                        Text("حفظ", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Wheel Time Picker
                    WheelTimePicker(
                        initialHour = hour,
                        initialMinute = minute,
                        is24Hour = is24Hour,
                        onTimeChanged = { h, m ->
                            hour = h
                            minute = m
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alarm Label TextField
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("اسم / ملاحظة المنبه") },
                        leadingIcon = {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = PrimaryPurple)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alarm_label_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedContainerColor = DarkSurfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = DarkSurfaceVariant.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Repeat Days Section
                    Text(
                        text = "أيام التكرار",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeekMap.forEach { (dayId, dayName) ->
                            val isSelected = selectedDays.contains(dayId)
                            Surface(
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayId
                                    } else {
                                        selectedDays + dayId
                                    }
                                },
                                shape = CircleShape,
                                color = if (isSelected) AccentOrange else DarkSurfaceVariant,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("day_button_$dayId")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = dayName.take(1),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else TextSecondaryDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Snooze duration & repeat count
                    Text(
                        text = "مدة الغفوة وعدد المرات",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(5, 10, 15).forEach { mins ->
                            val isSelected = snoozeMinutes == mins
                            Surface(
                                onClick = { snoozeMinutes = mins },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PrimaryPurple else DarkSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("snooze_${mins}_min")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$mins دقائق",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else TextSecondaryDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sound selection & Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نغمة المنبه",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        TextButton(
                            onClick = {
                                if (isPreviewingSound) {
                                    soundManager.stop()
                                    isPreviewingSound = false
                                } else {
                                    isPreviewingSound = true
                                    soundManager.playAlarm(
                                        soundId = selectedSound,
                                        isVibrate = false,
                                        isGradualVolume = false,
                                        isPreview = true,
                                        onStop = { isPreviewingSound = false }
                                    )
                                }
                            },
                            modifier = Modifier.testTag("preview_sound_button")
                        ) {
                            Icon(
                                imageVector = if (isPreviewingSound) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPreviewingSound) "إيقاف المعاينة" else "معاينة النغمة",
                                color = AccentCyan,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        soundOptions.forEach { (id, name) ->
                            val isSelected = selectedSound == id
                            Surface(
                                onClick = {
                                    selectedSound = id
                                    soundManager.stop()
                                    isPreviewingSound = true
                                    soundManager.playAlarm(
                                        soundId = id,
                                        isVibrate = false,
                                        isGradualVolume = false,
                                        isPreview = true,
                                        onStop = { isPreviewingSound = false }
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sound_option_$id")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else TextSecondaryDark
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Toggles: Gradual Volume & Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تدرج تصاعدي في الصوت",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "يبدأ هادئاً ويعلو تدريجياً لصحوة مريحة",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedDark
                            )
                        }
                        Switch(
                            checked = isGradualVolume,
                            onCheckedChange = { isGradualVolume = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "الاهتزاز",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "اهتزاز الجهاز مع نغمة المنبه",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedDark
                            )
                        }
                        Switch(
                            checked = isVibrate,
                            onCheckedChange = { isVibrate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dismiss Mission Selection
                    Text(
                        text = "مهمة إيقاف المنبه (تحدي الاستيقاظ)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اختر مهمة عقلية أو حركية لإجبارك على الاستيقاظ التام",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        missionOptions.forEach { (type, labelText) ->
                            val isSelected = selectedMission == type
                            Surface(
                                onClick = { selectedMission = type },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) AccentOrange.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AccentOrange) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mission_option_$type")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = labelText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else TextSecondaryDark
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AccentOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
