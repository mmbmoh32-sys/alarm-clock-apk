package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Alarm
import com.example.ui.theme.*
import com.example.util.TimeFormatter

@Composable
fun AlarmRingingScreen(
    alarm: Alarm,
    is24Hour: Boolean,
    onDismissAlarm: () -> Unit,
    onSnoozeAlarm: () -> Unit
) {
    var showMissionDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "ring_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val (timeText, amPmText) = remember(alarm.hour, alarm.minute, is24Hour) {
        TimeFormatter.formatTime(alarm.hour, alarm.minute, is24Hour)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PrimaryPurpleDark.copy(alpha = 0.8f),
                        DarkBackground
                    )
                )
            )
            .testTag("alarm_ringing_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Pulsing Alarm Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AccentOrange.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxSize()
                ) {}
                Surface(
                    shape = CircleShape,
                    color = AccentOrange,
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time and Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 64.sp
                        ),
                        color = Color.White
                    )
                    if (amPmText.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = amPmText,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentOrange,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = alarm.label.ifBlank { "حان وقت الاستيقاظ!" },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                if (alarm.missionType != "NONE") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Text(
                            text = "مهمة إيقاف: ${alarm.getMissionDisplayName()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentOrangeLight,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom Buttons: Snooze & Dismiss
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Snooze Button
                OutlinedButton(
                    onClick = onSnoozeAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("snooze_alarm_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple)
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "غفوة (${alarm.snoozeMinutes} دقائق)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Dismiss Button
                Button(
                    onClick = {
                        if (alarm.missionType == "NONE") {
                            onDismissAlarm()
                        } else {
                            showMissionDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("dismiss_alarm_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (alarm.missionType == "NONE") "إيقاف المنبه" else "بدء مهمة الإيقاف 🎯",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Mission Dialog if triggered
        if (showMissionDialog) {
            DismissMissionDialog(
                missionType = alarm.missionType,
                onMissionSuccess = {
                    showMissionDialog = false
                    onDismissAlarm()
                },
                onCancel = {
                    showMissionDialog = false
                }
            )
        }
    }
}
