package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AlarmRingingScreen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.TimeFormatter
import com.example.viewmodel.AlarmViewModel

enum class AppScreen(val titleAr: String) {
    Alarm("المنبه"),
    WorldClock("الساعة العالمية"),
    Bedtime("النوم"),
    Timer("المؤقت"),
    Stopwatch("ساعة الإيقاف")
}

class MainActivity : ComponentActivity() {
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "SYSTEM" -> isSystemInDarkTheme()
                else -> true
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                // Enforce RTL layout direction for Arabic
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AlarmAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AlarmAppContent(viewModel: AlarmViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.Alarm) }
    var showPrayersDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val timerPresets by viewModel.timerPresets.collectAsStateWithLifecycle()
    val ringingAlarm by viewModel.ringingAlarm.collectAsStateWithLifecycle()
    val is24Hour by viewModel.is24HourFormat.collectAsStateWithLifecycle()
    val favoriteCities by viewModel.favoriteCityIds.collectAsStateWithLifecycle()

    val remainingTimeText = remember(alarms) {
        TimeFormatter.getRemainingTimeDescription(alarms)
    }

    // BackHandler: navigate back to Alarm tab if on any other tab
    BackHandler(enabled = currentScreen != AppScreen.Alarm) {
        currentScreen = AppScreen.Alarm
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar")
            ) {
                // Tab 1: Alarm
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Alarm,
                    onClick = { currentScreen = AppScreen.Alarm },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.Alarm) Icons.Filled.Alarm else Icons.Outlined.Alarm,
                            contentDescription = "المنبه"
                        )
                    },
                    label = { Text("المنبه", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AccentOrange,
                        indicatorColor = AccentOrange.copy(alpha = 0.25f),
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_tab_alarm")
                )

                // Tab 2: World Clock
                NavigationBarItem(
                    selected = currentScreen == AppScreen.WorldClock,
                    onClick = { currentScreen = AppScreen.WorldClock },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.WorldClock) Icons.Filled.Public else Icons.Outlined.Public,
                            contentDescription = "الساعة العالمية"
                        )
                    },
                    label = { Text("العالمية", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = PrimaryPurpleLight,
                        indicatorColor = PrimaryPurple.copy(alpha = 0.25f),
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_tab_world_clock")
                )

                // Tab 3: Bedtime
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Bedtime,
                    onClick = { currentScreen = AppScreen.Bedtime },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.Bedtime) Icons.Filled.Bedtime else Icons.Outlined.Bedtime,
                            contentDescription = "وضع النوم"
                        )
                    },
                    label = { Text("النوم", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = PrimaryPurpleLight,
                        indicatorColor = PrimaryPurple.copy(alpha = 0.25f),
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_tab_bedtime")
                )

                // Tab 4: Timer
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Timer,
                    onClick = { currentScreen = AppScreen.Timer },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.Timer) Icons.Filled.HourglassBottom else Icons.Outlined.HourglassBottom,
                            contentDescription = "المؤقت"
                        )
                    },
                    label = { Text("المؤقت", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AccentCyan,
                        indicatorColor = AccentCyan.copy(alpha = 0.25f),
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_tab_timer")
                )

                // Tab 5: Stopwatch
                NavigationBarItem(
                    selected = currentScreen == AppScreen.Stopwatch,
                    onClick = { currentScreen = AppScreen.Stopwatch },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.Stopwatch) Icons.Filled.Timer else Icons.Outlined.Timer,
                            contentDescription = "ساعة الإيقاف"
                        )
                    },
                    label = { Text("الإيقاف", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AccentGreen,
                        indicatorColor = AccentGreen.copy(alpha = 0.25f),
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_tab_stopwatch")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                when (screen) {
                    AppScreen.Alarm -> {
                        AlarmScreen(
                            alarms = alarms,
                            remainingTimeText = remainingTimeText,
                            is24Hour = is24Hour,
                            soundManager = viewModel.soundManager,
                            onToggleAlarm = { alarm, enabled -> viewModel.toggleAlarm(alarm, enabled) },
                            onSaveAlarm = { alarm -> viewModel.saveAlarm(alarm) },
                            onDeleteAlarm = { alarm -> viewModel.deleteAlarm(alarm) },
                            onTestRingAlarm = { alarm -> viewModel.triggerAlarmRinging(alarm) },
                            onOpenPrayers = { showPrayersDialog = true },
                            onOpenSettings = { showSettingsDialog = true }
                        )
                    }

                    AppScreen.WorldClock -> {
                        WorldClockScreen(
                            favoriteCityIds = favoriteCities,
                            is24Hour = is24Hour,
                            onToggleFavoriteCity = { cityId -> viewModel.toggleFavoriteCity(cityId) }
                        )
                    }

                    AppScreen.Bedtime -> {
                        BedtimeScreen(
                            prefs = viewModel.prefs,
                            is24Hour = is24Hour,
                            soundManager = viewModel.soundManager
                        )
                    }

                    AppScreen.Timer -> {
                        TimerScreen(
                            presets = timerPresets,
                            soundManager = viewModel.soundManager,
                            onAddPreset = { preset -> viewModel.addTimerPreset(preset) },
                            onDeletePreset = { preset -> viewModel.deleteTimerPreset(preset) }
                        )
                    }

                    AppScreen.Stopwatch -> {
                        StopwatchScreen()
                    }
                }
            }

            // Dialogs
            if (showPrayersDialog) {
                PrayerTimesDialog(
                    viewModel = viewModel,
                    is24Hour = is24Hour,
                    soundManager = viewModel.soundManager,
                    onDismiss = { showPrayersDialog = false }
                )
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    prefs = viewModel.prefs,
                    onSettingsChanged = { viewModel.refreshPreferences() },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // Full Screen Ringing Overlay
            ringingAlarm?.let { activeAlarm ->
                AlarmRingingScreen(
                    alarm = activeAlarm,
                    is24Hour = is24Hour,
                    onDismissAlarm = { viewModel.dismissRingingAlarm() },
                    onSnoozeAlarm = { viewModel.snoozeRingingAlarm() }
                )
            }
        }
    }
}
