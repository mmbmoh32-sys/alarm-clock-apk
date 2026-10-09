package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AlarmSoundManager
import com.example.data.Alarm
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.TimerPreset
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val alarmDao = database.alarmDao()
    private val timerPresetDao = database.timerPresetDao()

    val prefs = PreferencesManager(application)
    val soundManager = AlarmSoundManager(application)

    val alarms: StateFlow<List<Alarm>> = alarmDao.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timerPresets: StateFlow<List<TimerPreset>> = timerPresetDao.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ringingAlarm = MutableStateFlow<Alarm?>(null)
    val ringingAlarm: StateFlow<Alarm?> = _ringingAlarm.asStateFlow()

    private val _is24HourFormat = MutableStateFlow(prefs.is24HourFormat)
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat.asStateFlow()

    private val _favoriteCityIds = MutableStateFlow(prefs.favoriteCityIds)
    val favoriteCityIds: StateFlow<Set<String>> = _favoriteCityIds.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private var lastTriggeredMinute = -1

    init {
        viewModelScope.launch {
            seedDefaultsIfEmpty()
        }
        // Background loop to check for alarm trigger time every 5 seconds
        viewModelScope.launch {
            while (true) {
                checkAlarmsTrigger()
                delay(5000)
            }
        }
    }

    private suspend fun seedDefaultsIfEmpty() {
        if (timerPresetDao.getCount() == 0) {
            val defaultPresets = listOf(
                TimerPreset(title = "تحضير الشاي 🫖", durationSeconds = 180, iconName = "tea"),
                TimerPreset(title = "سلق البيض 🥚", durationSeconds = 420, iconName = "egg"),
                TimerPreset(title = "جلسة تركيز بومودورو 🍅", durationSeconds = 1500, iconName = "focus"),
                TimerPreset(title = "قيلولة طاقة ⚡", durationSeconds = 1200, iconName = "nap"),
                TimerPreset(title = "تمارين رياضية 🏃", durationSeconds = 2700, iconName = "workout"),
                TimerPreset(title = "استراحة سريعة ⏸️", durationSeconds = 300, iconName = "break")
            )
            timerPresetDao.insertAll(defaultPresets)

            alarmDao.insertAlarm(
                Alarm(
                    hour = 5,
                    minute = 30,
                    isEnabled = true,
                    label = "استيقاظ لصلاة الفجر 🕌",
                    daysOfWeek = "1,2,3,4,5,6,7",
                    snoozeMinutes = 10,
                    soundId = "dawn",
                    isVibrate = true,
                    isVolumeGradual = true,
                    missionType = "MATH",
                    missionDifficulty = "EASY"
                )
            )
            alarmDao.insertAlarm(
                Alarm(
                    hour = 7,
                    minute = 0,
                    isEnabled = true,
                    label = "الاستعداد للعمل والنشاط ☕",
                    daysOfWeek = "1,2,3,4,5",
                    snoozeMinutes = 5,
                    soundId = "digital",
                    isVibrate = true,
                    isVolumeGradual = true,
                    missionType = "TYPING",
                    missionDifficulty = "EASY"
                )
            )
        }
    }

    private fun checkAlarmsTrigger() {
        val now = LocalDateTime.now()
        val currentMinuteOfDay = now.hour * 60 + now.minute

        if (currentMinuteOfDay == lastTriggeredMinute) return // Already triggered this minute

        val currentDayModel = when (now.dayOfWeek) {
            DayOfWeek.SUNDAY -> 1
            DayOfWeek.MONDAY -> 2
            DayOfWeek.TUESDAY -> 3
            DayOfWeek.WEDNESDAY -> 4
            DayOfWeek.THURSDAY -> 5
            DayOfWeek.FRIDAY -> 6
            DayOfWeek.SATURDAY -> 7
        }

        val activeList = alarms.value.filter { it.isEnabled }
        for (alarm in activeList) {
            if (alarm.hour == now.hour && alarm.minute == now.minute) {
                val days = alarm.getDaysList()
                if (days.isEmpty() || days.contains(currentDayModel)) {
                    lastTriggeredMinute = currentMinuteOfDay
                    triggerAlarmRinging(alarm)
                    break
                }
            }
        }
    }

    fun triggerAlarmRinging(alarm: Alarm) {
        _ringingAlarm.value = alarm
        soundManager.playAlarm(
            soundId = alarm.soundId,
            isVibrate = alarm.isVibrate,
            isGradualVolume = alarm.isVolumeGradual,
            isPreview = false
        )
    }

    fun dismissRingingAlarm() {
        val current = _ringingAlarm.value
        soundManager.stop()
        _ringingAlarm.value = null

        if (current != null && !current.isRepeating()) {
            // Turn off one-time alarm after it rings
            viewModelScope.launch {
                alarmDao.setAlarmEnabled(current.id, false)
            }
        }
    }

    fun snoozeRingingAlarm() {
        val current = _ringingAlarm.value ?: return
        soundManager.stop()
        _ringingAlarm.value = null

        viewModelScope.launch {
            // Set alarm for snoozeMinutes later
            val now = LocalTime.now().plusMinutes(current.snoozeMinutes.toLong())
            val snoozedAlarm = current.copy(
                hour = now.hour,
                minute = now.minute,
                currentSnoozeCount = current.currentSnoozeCount + 1
            )
            alarmDao.updateAlarm(snoozedAlarm)
        }
    }

    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            alarmDao.setAlarmEnabled(alarm.id, isEnabled)
        }
    }

    fun saveAlarm(alarm: Alarm) {
        viewModelScope.launch {
            if (alarm.id == 0L) {
                alarmDao.insertAlarm(alarm)
            } else {
                alarmDao.updateAlarm(alarm)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmDao.deleteAlarm(alarm)
        }
    }

    fun addTimerPreset(preset: TimerPreset) {
        viewModelScope.launch {
            timerPresetDao.insertPreset(preset)
        }
    }

    fun deleteTimerPreset(preset: TimerPreset) {
        viewModelScope.launch {
            timerPresetDao.deletePreset(preset)
        }
    }

    fun toggleFavoriteCity(cityId: String) {
        val current = _favoriteCityIds.value.toMutableSet()
        if (current.contains(cityId)) {
            current.remove(cityId)
        } else {
            current.add(cityId)
        }
        _favoriteCityIds.value = current
        prefs.favoriteCityIds = current
    }

    fun refreshPreferences() {
        _is24HourFormat.value = prefs.is24HourFormat
        _themeMode.value = prefs.themeMode
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stop()
    }
}
