package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_alarm_prefs", Context.MODE_PRIVATE)

    var is24HourFormat: Boolean
        get() = prefs.getBoolean("is_24_hour", false)
        set(value) = prefs.edit().putBoolean("is_24_hour", value).apply()

    var defaultSnoozeMinutes: Int
        get() = prefs.getInt("default_snooze_min", 10)
        set(value) = prefs.edit().putInt("default_snooze_min", value).apply()

    var weekStartSaturday: Boolean
        get() = prefs.getBoolean("week_start_sat", true)
        set(value) = prefs.edit().putBoolean("week_start_sat", value).apply()

    var themeMode: String // "DARK", "LIGHT", "SYSTEM"
        get() = prefs.getString("theme_mode", "DARK") ?: "DARK"
        set(value) = prefs.edit().putString("theme_mode", value).apply()

    var bedtimeHour: Int
        get() = prefs.getInt("bedtime_hour", 23)
        set(value) = prefs.edit().putInt("bedtime_hour", value).apply()

    var bedtimeMinute: Int
        get() = prefs.getInt("bedtime_minute", 0)
        set(value) = prefs.edit().putInt("bedtime_minute", value).apply()

    var wakeUpHour: Int
        get() = prefs.getInt("wakeup_hour", 7)
        set(value) = prefs.edit().putInt("wakeup_hour", value).apply()

    var wakeUpMinute: Int
        get() = prefs.getInt("wakeup_minute", 0)
        set(value) = prefs.edit().putInt("wakeup_minute", value).apply()

    var isBedtimeReminderEnabled: Boolean
        get() = prefs.getBoolean("bedtime_reminder_enabled", true)
        set(value) = prefs.edit().putBoolean("bedtime_reminder_enabled", value).apply()

    var favoriteCityIds: Set<String>
        get() = prefs.getStringSet("favorite_cities", setOf("makkah", "jerusalem", "cairo", "dubai", "london", "tokyo")) ?: emptySet()
        set(value) = prefs.edit().putStringSet("favorite_cities", value).apply()

    var selectedPrayerCityId: String
        get() = prefs.getString("prayer_city_id", "makkah") ?: "makkah"
        set(value) = prefs.edit().putString("prayer_city_id", value).apply()

    // Location & GPS Settings
    var isUseGpsLocation: Boolean
        get() = prefs.getBoolean("prayer_use_gps", true)
        set(value) = prefs.edit().putBoolean("prayer_use_gps", value).apply()

    var userLatitude: Double
        get() = java.lang.Double.longBitsToDouble(prefs.getLong("prayer_user_lat", java.lang.Double.doubleToLongBits(21.4225)))
        set(value) = prefs.edit().putLong("prayer_user_lat", java.lang.Double.doubleToLongBits(value)).apply()

    var userLongitude: Double
        get() = java.lang.Double.longBitsToDouble(prefs.getLong("prayer_user_lng", java.lang.Double.doubleToLongBits(39.8262)))
        set(value) = prefs.edit().putLong("prayer_user_lng", java.lang.Double.doubleToLongBits(value)).apply()

    var locationDisplayName: String
        get() = prefs.getString("prayer_location_name", "مكة المكرمة (الموقع الافتراضي)") ?: "مكة المكرمة"
        set(value) = prefs.edit().putString("prayer_location_name", value).apply()

    // Prayer Athan Alert settings
    fun isPrayerAthanEnabled(prayerId: String): Boolean {
        // Sunrise is default off for athan, others default on
        val defaultVal = prayerId != "sunrise"
        return prefs.getBoolean("athan_enabled_$prayerId", defaultVal)
    }

    fun setPrayerAthanEnabled(prayerId: String, enabled: Boolean) {
        prefs.edit().putBoolean("athan_enabled_$prayerId", enabled).apply()
    }

    fun getPrayerMuadhin(prayerId: String): String {
        return prefs.getString("athan_muadhin_$prayerId", "athan_makkah") ?: "athan_makkah"
    }

    fun setPrayerMuadhin(prayerId: String, muadhinId: String) {
        prefs.edit().putString("athan_muadhin_$prayerId", muadhinId).apply()
    }
}
