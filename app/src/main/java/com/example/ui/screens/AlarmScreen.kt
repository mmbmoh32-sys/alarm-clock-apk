package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audio.AlarmSoundManager
import com.example.data.Alarm
import com.example.ui.components.AlarmCard
import com.example.ui.components.AlarmEditDialog
import com.example.ui.components.DigitalClockHeader
import com.example.ui.theme.*

@Composable
fun AlarmScreen(
    alarms: List<Alarm>,
    remainingTimeText: String?,
    is24Hour: Boolean,
    soundManager: AlarmSoundManager,
    onToggleAlarm: (Alarm, Boolean) -> Unit,
    onSaveAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onTestRingAlarm: (Alarm) -> Unit,
    onOpenPrayers: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alarmBeingEdited by remember { mutableStateOf<Alarm?>(null) }
    var isCreatingNewAlarm by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("alarm_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Digital Clock & Smart Pill Header
            item {
                DigitalClockHeader(
                    remainingTimeText = remainingTimeText,
                    is24Hour = is24Hour,
                    onOpenPrayers = onOpenPrayers,
                    onOpenSettings = onOpenSettings
                )
            }

            // Alarms Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المنبهات المجدولة",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    val activeCount = alarms.count { it.isEnabled }
                    Text(
                        text = "$activeCount نشط من ${alarms.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondaryDark
                    )
                }
            }

            // Empty state if no alarms
            if (alarms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlarmAdd,
                                contentDescription = null,
                                tint = PrimaryPurpleLight,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "لا يوجد أي منبه حتى الآن",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اضغط على زر (+) بالأسفل لإضافة منبهك الأول مع مهام استيقاظ ذكية ونغمات ممتعة",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // List of alarms
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        is24Hour = is24Hour,
                        onToggle = { isEnabled -> onToggleAlarm(alarm, isEnabled) },
                        onEdit = { alarmBeingEdited = alarm },
                        onDelete = { onDeleteAlarm(alarm) },
                        onTestRing = { onTestRingAlarm(alarm) }
                    )
                }
            }
        }

        // Floating Action Button (+)
        FloatingActionButton(
            onClick = { isCreatingNewAlarm = true },
            containerColor = AccentOrange,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart) // RTL placement
                .padding(24.dp)
                .size(60.dp)
                .testTag("add_alarm_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "إضافة منبه جديد",
                modifier = Modifier.size(30.dp)
            )
        }

        // Add / Edit Alarm Dialog
        if (isCreatingNewAlarm || alarmBeingEdited != null) {
            AlarmEditDialog(
                alarmToEdit = alarmBeingEdited,
                is24Hour = is24Hour,
                soundManager = soundManager,
                onDismiss = {
                    isCreatingNewAlarm = false
                    alarmBeingEdited = null
                },
                onSave = { savedAlarm ->
                    onSaveAlarm(savedAlarm)
                    isCreatingNewAlarm = false
                    alarmBeingEdited = null
                }
            )
        }
    }
}
