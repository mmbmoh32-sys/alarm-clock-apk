package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelTimePicker(
    initialHour: Int, // 0..23
    initialMinute: Int, // 0..59
    is24Hour: Boolean,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHour24 by remember(initialHour) { mutableIntStateOf(initialHour) }
    var selectedMinute by remember(initialMinute) { mutableIntStateOf(initialMinute) }
    var isAm by remember(initialHour) { mutableStateOf(initialHour < 12) }

    val displayHour = remember(selectedHour24, is24Hour) {
        if (is24Hour) selectedHour24
        else when (val h = selectedHour24 % 12) {
            0 -> 12
            else -> h
        }
    }

    val hoursList = remember(is24Hour) {
        if (is24Hour) (0..23).toList() else (1..12).toList()
    }
    val minutesList = remember { (0..59).toList() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("wheel_time_picker"),
        contentAlignment = Alignment.Center
    ) {
        // Center highlighted selection bar
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            color = PrimaryPurple.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple.copy(alpha = 0.6f))
        ) {}

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour Wheel
            NumberWheel(
                items = hoursList,
                selectedItem = displayHour,
                itemHeight = 52.dp,
                onItemSelected = { hourSelected ->
                    val newHour24 = if (is24Hour) {
                        hourSelected
                    } else {
                        if (isAm) {
                            if (hourSelected == 12) 0 else hourSelected
                        } else {
                            if (hourSelected == 12) 12 else hourSelected + 12
                        }
                    }
                    selectedHour24 = newHour24
                    onTimeChanged(selectedHour24, selectedMinute)
                },
                modifier = Modifier.weight(1f),
                testTag = "hour_wheel"
            )

            // Colon separator
            Text(
                text = ":",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = PrimaryPurple,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Minute Wheel
            NumberWheel(
                items = minutesList,
                selectedItem = selectedMinute,
                itemHeight = 52.dp,
                formatPattern = "%02d",
                onItemSelected = { minuteSelected ->
                    selectedMinute = minuteSelected
                    onTimeChanged(selectedHour24, selectedMinute)
                },
                modifier = Modifier.weight(1f),
                testTag = "minute_wheel"
            )

            // AM / PM Selector for 12-hour mode
            if (!is24Hour) {
                Spacer(modifier = Modifier.width(12.dp))
                AmPmSelector(
                    isAm = isAm,
                    onToggle = { newIsAm ->
                        isAm = newIsAm
                        val current12 = when (val h = selectedHour24 % 12) {
                            0 -> 12
                            else -> h
                        }
                        selectedHour24 = if (newIsAm) {
                            if (current12 == 12) 0 else current12
                        } else {
                            if (current12 == 12) 12 else current12 + 12
                        }
                        onTimeChanged(selectedHour24, selectedMinute)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NumberWheel(
    items: List<Int>,
    selectedItem: Int,
    itemHeight: Dp,
    formatPattern: String = "%02d",
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val initialIndex = items.indexOf(selectedItem).coerceAtLeast(0)
    // Repeat items to simulate continuous circular scrolling
    val repeatCount = 100
    val totalItems = items.size * repeatCount
    val initialScrollIndex = (repeatCount / 2) * items.size + initialIndex

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex - 1)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex + 1
            val realIndex = centerIndex % items.size
            if (realIndex in items.indices) {
                onItemSelected(items[realIndex])
            }
        }
    }

    Box(
        modifier = modifier
            .height(160.dp)
            .testTag(testTag)
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(totalItems) { index ->
                val item = items[index % items.size]
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.US, formatPattern, item),
                        style = if (isSelected) {
                            MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        color = if (isSelected) Color.White else TextMutedDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(if (isSelected) 1f else 0.45f)
                    )
                }
            }
        }

        // Top gradient fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkSurface, Color.Transparent)
                    )
                )
        )

        // Bottom gradient fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DarkSurface)
                    )
                )
        )
    }
}

@Composable
private fun AmPmSelector(
    isAm: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(60.dp)
            .height(110.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = { onToggle(true) },
            shape = RoundedCornerShape(12.dp),
            color = if (isAm) AccentOrange else DarkSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("am_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "ص",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isAm) Color.White else TextSecondaryDark
                )
            }
        }

        Surface(
            onClick = { onToggle(false) },
            shape = RoundedCornerShape(12.dp),
            color = if (!isAm) AccentOrange else DarkSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("pm_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "م",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (!isAm) Color.White else TextSecondaryDark
                )
            }
        }
    }
}
