package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        targetValue = if (alarm.isEnabled) AccentOrange.copy(alpha = 0.55f) else DarkSurfaceVariant,
        label = "border_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(26.dp))
            .clickable { onEdit() }
            .testTag("alarm_card_${alarm.id}"),
        shape = RoundedCornerShape(26.dp),
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
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Time, AM/PM, and Toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp
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

                Spacer(modifier = Modifier.height(4.dp))

                // Alarm Label
                Text(
                    text = alarm.label.ifBlank { "منبه" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (alarm.isEnabled) TextPrimaryDark else TextMutedDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Horizontal subtle divider for structured modern aesthetic
                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = DarkSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Footer: Badges on leading side (Right) & Action Buttons on trailing side (Left)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Feature Badges: Repeat Days & Mission (Never wraps awkwardly)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Repeat days badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = if (alarm.isEnabled) PrimaryPurpleLight else TextMutedDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = alarm.getFormattedDaysArabic(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (alarm.isEnabled) TextSecondaryDark else TextMutedDark,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Mission badge (if enabled)
                        if (alarm.missionType != "NONE") {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = alarm.getMissionDisplayName(),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = AccentOrangeLight,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Action buttons (Test Ring & Delete) - Beautifully styled and spaced
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play / Test Ring Button
                        Surface(
                            onClick = onTestRing,
                            shape = RoundedCornerShape(13.dp),
                            color = PrimaryPurple.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("test_ring_${alarm.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "تجربة الرنين",
                                    tint = PrimaryPurpleLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Delete Button
                        Surface(
                            onClick = onDelete,
                            shape = RoundedCornerShape(13.dp),
                            color = AccentRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.38f)),
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("delete_alarm_${alarm.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "حذف المنبه",
                                    tint = AccentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
