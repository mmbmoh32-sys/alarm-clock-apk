package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ui.theme.*
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay

data class StopwatchLap(
    val lapIndex: Int,
    val lapTimeMillis: Long,
    val splitTimeMillis: Long
)

@Composable
fun StopwatchScreen(modifier: Modifier = Modifier) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<StopwatchLap>() }
    var lastLapMarkMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - elapsedMillis
            while (isRunning) {
                elapsedMillis = System.currentTimeMillis() - startTime
                delay(30) // ~30 fps update
            }
        }
    }

    val fastestLapMillis = remember(laps.size) {
        if (laps.size >= 2) laps.minOfOrNull { it.lapTimeMillis } else null
    }
    val slowestLapMillis = remember(laps.size) {
        if (laps.size >= 2) laps.maxOfOrNull { it.lapTimeMillis } else null
    }

    val minutes = (elapsedMillis / 60000) % 60
    val seconds = (elapsedMillis / 1000) % 60
    val millis = (elapsedMillis % 1000) / 10

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("stopwatch_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ساعة الإيقاف",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "قياس الوقت وتسجيل اللفات بدقة أجزاء الثانية",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Stopwatch Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 62.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = String.format(java.util.Locale.US, ".%02d", millis),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = AccentOrange,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Control Buttons: Reset, Lap, Start/Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    FilledIconButton(
                        onClick = {
                            isRunning = false
                            elapsedMillis = 0L
                            lastLapMarkMillis = 0L
                            laps.clear()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("reset_stopwatch_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط", tint = TextSecondaryDark)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Start / Pause Main Button
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AccentOrange else PrimaryPurple
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("start_pause_stopwatch_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "إيقاف مؤقت" else "بدء",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Lap Button
                    FilledIconButton(
                        onClick = {
                            if (isRunning) {
                                val currentSplit = elapsedMillis
                                val currentLap = currentSplit - lastLapMarkMillis
                                lastLapMarkMillis = currentSplit
                                laps.add(
                                    0, // Add to top
                                    StopwatchLap(
                                        lapIndex = laps.size + 1,
                                        lapTimeMillis = currentLap,
                                        splitTimeMillis = currentSplit
                                    )
                                )
                            }
                        },
                        enabled = isRunning,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = DarkSurfaceVariant,
                            disabledContainerColor = DarkSurfaceCard
                        ),
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("lap_stopwatch_button")
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = "تسجيل لفة",
                            tint = if (isRunning) PrimaryPurpleLight else TextMutedDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Laps Header
        if (laps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "رقم اللفة",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMutedDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "وقت اللفة",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMutedDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "الوقت الإجمالي",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMutedDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Laps List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(laps, key = { _, lap -> lap.lapIndex }) { _, lap ->
                    val isFastest = fastestLapMillis != null && lap.lapTimeMillis == fastestLapMillis
                    val isSlowest = slowestLapMillis != null && lap.lapTimeMillis == slowestLapMillis

                    val lapColor = when {
                        isFastest -> AccentGreen
                        isSlowest -> AccentRed
                        else -> Color.White
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurface,
                        border = when {
                            isFastest -> androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f))
                            isSlowest -> androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                            else -> null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "لفة ${lap.lapIndex}" + if (isFastest) " (الأسرع ⚡)" else if (isSlowest) " (الأبطأ)" else "",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = lapColor,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = TimeFormatter.formatStopwatch(lap.lapTimeMillis),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = lapColor,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = TimeFormatter.formatStopwatch(lap.splitTimeMillis),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "ابدأ التشغيل واضغط على أيقونة العلم 🏁 لتسجيل اللفات",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedDark
            )
        }
    }
}
