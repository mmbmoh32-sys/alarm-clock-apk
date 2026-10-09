package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.Alarm
import com.example.ui.theme.*
import com.example.util.TimeFormatter

@Composable
fun AlarmCard(
    alarm: Alarm,
    is24Hour: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestRing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (timeText, amPmText) = remember(alarm.hour, alarm.minute, is24Hour) {
        TimeFormatter.formatTime(alarm.hour, alarm.minute, is24Hour)
    }

    val cardBorderColor by animateColorAsState(
        targetValue = if (alarm.isEnabled) AccentOrange.copy(alpha = 0.5f) else Color.Transparent,
        label = "border_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
            .clickable { onEdit() }
            .testTag("alarm_card_${alarm.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alarm.isEnabled) 4.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (alarm.isEnabled) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                AccentOrange.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Time, AM/PM, and Toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 38.sp
                            ),
                            color = if (alarm.isEnabled) Color.White else TextMutedDark
                        )
                        if (amPmText.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = amPmText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (alarm.isEnabled) AccentOrange else TextMutedDark,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentOrange,
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Alarm Label
                Text(
                    text = alarm.label.ifBlank { "منبه" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (alarm.isEnabled) TextPrimaryDark else TextMutedDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Badges and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Feature Badges: Repeat Days, Mission, Sound
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Repeat days badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = if (alarm.isEnabled) PrimaryPurpleLight else TextMutedDark,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = alarm.getFormattedDaysArabic(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (alarm.isEnabled) TextSecondaryDark else TextMutedDark
                                )
                            }
                        }

                        // Mission badge if enabled
                        if (alarm.missionType != "NONE") {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val missionIcon = when (alarm.missionType) {
                                        "MATH" -> Icons.Default.Calculate
                                        "SHAKE" -> Icons.Default.Vibration
                                        "TYPING" -> Icons.Default.Keyboard
                                        else -> Icons.Default.TaskAlt
                                    }
                                    Icon(
                                        imageVector = missionIcon,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = alarm.getMissionDisplayName(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentOrangeLight
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons (Test Ring & Delete)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onTestRing,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .testTag("test_ring_${alarm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "تجربة الرنين",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .testTag("delete_alarm_${alarm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "حذف المنبه",
                                tint = AccentRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
