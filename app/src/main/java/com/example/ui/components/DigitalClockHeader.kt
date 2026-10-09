package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.theme.*
import com.example.util.HijriDateUtil
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DigitalClockHeader(
    remainingTimeText: String?,
    is24Hour: Boolean,
    onOpenPrayers: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    // Live tick every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    val hour = currentTime.hour
    val minute = currentTime.minute
    val second = currentTime.second

    val formattedHour = if (is24Hour) {
        String.format(Locale.US, "%02d", hour)
    } else {
        val h = when (hour) {
            0 -> 12
            in 1..12 -> hour
            else -> hour - 12
        }
        String.format(Locale.US, "%02d", h)
    }
    val formattedMinute = String.format(Locale.US, "%02d", minute)
    val formattedSecond = String.format(Locale.US, "%02d", second)
    val amPm = if (hour < 12) "ص" else "م"

    val todayGregorian = remember { HijriDateUtil.getTodayGregorianArabic() }
    val todayHijri = remember { HijriDateUtil.getTodayHijriArabic() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("digital_clock_header"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryPurple.copy(alpha = 0.15f),
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
                // Top Row: Prayers button and Settings button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prayer times quick button
                    Surface(
                        onClick = onOpenPrayers,
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurfaceVariant,
                        modifier = Modifier.testTag("open_prayers_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = "مواقيت الصلاة",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مواقيت الصلاة",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimaryDark
                            )
                        }
                    }

                    // Settings button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Clock Display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$formattedHour:$formattedMinute",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 62.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.padding(bottom = 10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (!is24Hour) {
                            Text(
                                text = amPm,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AccentOrange
                            )
                        }
                        Text(
                            text = ":$formattedSecond",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextMutedDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date displays
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = todayGregorian,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = " • ",
                        color = TextMutedDark,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = todayHijri,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = PrimaryPurpleLight
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Smart Alarm countdown badge
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (remainingTimeText != null) DarkSurfaceVariant else DarkSurfaceCard,
                        border = if (remainingTimeText != null) {
                            androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.35f))
                        } else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (remainingTimeText != null) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (remainingTimeText != null) AccentOrange else TextMutedDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = remainingTimeText ?: "لا توجد منبهات قادمة مفعّلة",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (remainingTimeText != null) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (remainingTimeText != null) AccentOrangeLight else TextMutedDark
                            )
                        }
                    }
                }
            }
        }
    }
}
