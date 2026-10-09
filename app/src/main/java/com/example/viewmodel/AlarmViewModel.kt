package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AlarmSoundManager
import com.example.data.Alarm
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.TimerPreset
import com.example.util.LocationHelper
import com.example.util.PrayerTime
import com.example.util.PrayerTimesCalculator
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

    // Location & Prayer state
    private val _userLat = MutableStateFlow(prefs.userLatitude)
    val userLat: StateFlow<Double> = _userLat.asStateFlow()

    private val _userLng = MutableStateFlow(prefs.userLongitude)
    val userLng: StateFlow<Double> = _userLng.asStateFlow()

    private val _locationDisplayName = MutableStateFlow(prefs.locationDisplayName)
    val locationDisplayName: StateFlow<String> = _locationDisplayName.asStateFlow()

    private val _prayerTimesState = MutableStateFlow<List<PrayerTime>>(emptyList())
    val prayerTimesState: StateFlow<List<PrayerTime>> = _prayerTimesState.asStateFlow()

    private var lastTriggeredMinute = -1

    init {
        viewModelScope.launch {
            seedDefaultsIfEmpty()
            refreshPrayerTimes()
        }
        // Background loop to check for alarm & athan triggers every 5 seconds
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
                    soundId = "athan_makkah",
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

    fun refreshPrayerTimes() {
        val list = PrayerTimesCalculator.calculatePrayers(
            lat = _userLat.value,
            lng = _userLng.value,
            is24Hour = _is24HourFormat.value,
            athanEnabledProvider = { prefs.isPrayerAthanEnabled(it) },
            muadhinProvider = { prefs.getPrayerMuadhin(it) }
        )
        _prayerTimesState.value = list
    }

    private fun checkAlarmsTrigger() {
        val now = LocalDateTime.now()
        val currentMinuteOfDay = now.hour * 60 + now.minute

        if (currentMinuteOfDay == lastTriggeredMinute) return

        val currentDayModel = when (now.dayOfWeek) {
            DayOfWeek.SUNDAY -> 1
            DayOfWeek.MONDAY -> 2
            DayOfWeek.TUESDAY -> 3
            DayOfWeek.WEDNESDAY -> 4
            DayOfWeek.THURSDAY -> 5
            DayOfWeek.FRIDAY -> 6
            DayOfWeek.SATURDAY -> 7
        }

        // 1. Check Standard Alarms
        val activeList = alarms.value.filter { it.isEnabled }
        for (alarm in activeList) {
            if (alarm.hour == now.hour && alarm.minute == now.minute) {
                val days = alarm.getDaysList()
                if (days.isEmpty() || days.contains(currentDayModel)) {
                    lastTriggeredMinute = currentMinuteOfDay
                    triggerAlarmRinging(alarm)
                    return
                }
            }
        }

        // 2. Check Prayer Athan Alerts
        val currentPrayers = _prayerTimesState.value
        for (prayer in currentPrayers) {
            if (prayer.isAthanEnabled && prayer.id != "sunrise") {
                if (prayer.time.hour == now.hour && prayer.time.minute == now.minute) {
                    lastTriggeredMinute = currentMinuteOfDay
                    val athanAlarm = Alarm(
                        id = 888800L + prayer.id.hashCode(),
                        hour = prayer.time.hour,
                        minute = prayer.time.minute,
                        isEnabled = true,
                        label = "حان الآن موعد أذان ${prayer.nameAr} 🕌",
                        soundId = prayer.muadhinId,
                        isVibrate = true,
                        isVolumeGradual = false,
                        missionType = "NONE"
                    )
                    triggerAlarmRinging(athanAlarm)
                    return
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

        if (current != null && current.id < 880000L && !current.isRepeating()) {
            viewModelScope.launch {
                alarmDao.setAlarmEnabled(current.id, false)
            }
        }
    }

    fun snoozeRingingAlarm() {
        val current = _ringingAlarm.value ?: return
        soundManager.stop()
        _ringingAlarm.value = null

        if (current.id < 880000L) {
            viewModelScope.launch {
                val now = LocalTime.now().plusMinutes(current.snoozeMinutes.toLong())
                val snoozedAlarm = current.copy(
                    hour = now.hour,
                    minute = now.minute,
                    currentSnoozeCount = current.currentSnoozeCount + 1
                )
                alarmDao.updateAlarm(snoozedAlarm)
            }
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

    fun requestGpsLocation(context: Context, onComplete: ((Boolean, String) -> Unit)? = null) {
        LocationHelper.fetchCurrentLocation(
            context = context,
            onSuccess = { lat, lng, cityName ->
                _userLat.value = lat
                _userLng.value = lng
                _locationDisplayName.value = cityName
                prefs.userLatitude = lat
                prefs.userLongitude = lng
                prefs.locationDisplayName = cityName
                refreshPrayerTimes()
                onComplete?.invoke(true, cityName)
            },
            onError = { errorMsg ->
                onComplete?.invoke(false, errorMsg)
            }
        )
    }

    fun setPrayerCityPreset(cityId: String) {
        val coords = PrayerTimesCalculator.cityCoordinates[cityId]
        val cityName = when (cityId) {
            "makkah" -> "مكة المكرمة"
            "madinah" -> "المدينة المنورة"
            "jerusalem" -> "القدس الشريف"
            "cairo" -> "القاهرة"
            "riyadh" -> "الرياض"
            "dubai" -> "دبي"
            "amman" -> "عمّان"
            "doha" -> "الدوحة"
            "kuwait" -> "الكويت"
            "london" -> "لندن"
            else -> "مكة المكرمة"
        }
        if (coords != null) {
            _userLat.value = coords.first
            _userLng.value = coords.second
            _locationDisplayName.value = cityName
            prefs.userLatitude = coords.first
            prefs.userLongitude = coords.second
            prefs.locationDisplayName = cityName
            prefs.selectedPrayerCityId = cityId
            refreshPrayerTimes()
        }
    }

    fun togglePrayerAthan(prayerId: String, isEnabled: Boolean) {
        prefs.setPrayerAthanEnabled(prayerId, isEnabled)
        refreshPrayerTimes()
    }

    fun setPrayerMuadhin(prayerId: String, muadhinId: String) {
        prefs.setPrayerMuadhin(prayerId, muadhinId)
        refreshPrayerTimes()
    }

    fun refreshPreferences() {
        _is24HourFormat.value = prefs.is24HourFormat
        _themeMode.value = prefs.themeMode
        refreshPrayerTimes()
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stop()
    }
}
