package com.example.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

data class PrayerTime(
    val id: String, // "fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"
    val nameAr: String,
    val nameEn: String,
    val time: LocalTime,
    val isNext: Boolean = false,
    val isAthanEnabled: Boolean = true,
    val muadhinId: String = "athan_makkah"
) {
    fun getFormattedTime(is24Hour: Boolean = false): String {
        val pattern = if (is24Hour) "HH:mm" else "hh:mm"
        val timeStr = time.format(DateTimeFormatter.ofPattern(pattern, Locale.US))
        val amPm = if (time.hour < 12) "ص" else "م"
        return if (is24Hour) timeStr else "$timeStr $amPm"
    }

    fun getMuadhinDisplayName(): String {
        return when (muadhinId) {
            "athan_makkah" -> "أذان الحرم المكي 🕋"
            "athan_madinah" -> "أذان المسجد النبوي 🕌"
            "athan_aqsa" -> "أذان المسجد الأقصى 🇵🇸"
            "athan_cairo" -> "أذان مصر التاريخي 🇪🇬"
            "athan_gentle" -> "تكبيرات وأذان هادئ 🕊️"
            else -> "أذان الحرم المكي 🕋"
        }
    }
}

object PrayerTimesCalculator {
    // Preset coordinates for major cities
    val cityCoordinates = mapOf(
        "makkah" to Pair(21.4225, 39.8262),
        "madinah" to Pair(24.4672, 39.6111),
        "jerusalem" to Pair(31.7683, 35.2137),
        "cairo" to Pair(30.0444, 31.2357),
        "dubai" to Pair(25.2048, 55.2708),
        "riyadh" to Pair(24.7136, 46.6753),
        "amman" to Pair(31.9454, 35.9284),
        "doha" to Pair(25.2854, 51.5310),
        "kuwait" to Pair(29.3759, 47.9774),
        "london" to Pair(51.5074, -0.1278),
        "tokyo" to Pair(35.6762, 139.6503)
    )

    val availableMuadhins = listOf(
        "athan_makkah" to "أذان الحرم المكي الشريف 🕋",
        "athan_madinah" to "أذان المسجد النبوي الشريف 🕌",
        "athan_aqsa" to "أذان المسجد الأقصى المبارك 🇵🇸",
        "athan_cairo" to "أذان مصر التاريخي 🇪🇬",
        "athan_gentle" to "تكبيرات وأذان هادئ 🕊️"
    )

    fun calculatePrayers(
        lat: Double,
        lng: Double,
        is24Hour: Boolean = false,
        athanEnabledProvider: ((String) -> Boolean)? = null,
        muadhinProvider: ((String) -> String)? = null
    ): List<PrayerTime> {
        val today = LocalDate.now()
        val dayOfYear = today.dayOfYear

        // Solar declination & equation of time approximation
        val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // minutes
        val declination = 23.45 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81))) // degrees

        // Approximate timezone offset from longitude (15 deg per hour)
        val timeZoneOffset = (round(lng / 15.0)).toInt()

        // Solar noon in hours
        val solarNoon = 12.0 + (timeZoneOffset * 15.0 - lng) / 15.0 - (eot / 60.0)

        fun hourAngle(angleDegrees: Double): Double {
            val latRad = Math.toRadians(lat)
            val decRad = Math.toRadians(declination)
            val zenRad = Math.toRadians(90.0 + angleDegrees)
            val cosH = (cos(zenRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            val clamped = cosH.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) / 15.0
        }

        val fajrAngle = 18.5 // Umm al-Qura standard
        val ishaAngle = 17.5

        val fajrHA = hourAngle(fajrAngle)
        val sunriseHA = hourAngle(0.833)
        val maghribHA = sunriseHA

        // Asr angle (standard Shafi'i / Maliki / Hanbali shadow factor = 1)
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(declination)
        val asrZenith = Math.toDegrees(atan(1.0 + tan(abs(latRad - decRad))))
        val asrHA = hourAngle(asrZenith - 90.0)

        fun toLocalTime(decimalHours: Double): LocalTime {
            var h = decimalHours
            while (h < 0) h += 24.0
            while (h >= 24) h -= 24.0
            val hour = h.toInt()
            val min = ((h - hour) * 60).toInt().coerceIn(0, 59)
            return LocalTime.of(hour, min)
        }

        val fajr = toLocalTime(solarNoon - fajrHA)
        val sunrise = toLocalTime(solarNoon - sunriseHA)
        val dhuhr = toLocalTime(solarNoon)
        val asr = toLocalTime(solarNoon + asrHA)
        val maghrib = toLocalTime(solarNoon + maghribHA)
        val isha = toLocalTime(solarNoon + hourAngle(ishaAngle))

        val now = LocalTime.now()

        val rawList = listOf(
            PrayerTime(
                id = "fajr",
                nameAr = "الفجر",
                nameEn = "Fajr",
                time = fajr,
                isAthanEnabled = athanEnabledProvider?.invoke("fajr") ?: true,
                muadhinId = muadhinProvider?.invoke("fajr") ?: "athan_makkah"
            ),
            PrayerTime(
                id = "sunrise",
                nameAr = "الشروق",
                nameEn = "Sunrise",
                time = sunrise,
                isAthanEnabled = athanEnabledProvider?.invoke("sunrise") ?: false,
                muadhinId = muadhinProvider?.invoke("sunrise") ?: "athan_gentle"
            ),
            PrayerTime(
                id = "dhuhr",
                nameAr = "الظهر",
                nameEn = "Dhuhr",
                time = dhuhr,
                isAthanEnabled = athanEnabledProvider?.invoke("dhuhr") ?: true,
                muadhinId = muadhinProvider?.invoke("dhuhr") ?: "athan_madinah"
            ),
            PrayerTime(
                id = "asr",
                nameAr = "العصر",
                nameEn = "Asr",
                time = asr,
                isAthanEnabled = athanEnabledProvider?.invoke("asr") ?: true,
                muadhinId = muadhinProvider?.invoke("asr") ?: "athan_makkah"
            ),
            PrayerTime(
                id = "maghrib",
                nameAr = "المغرب",
                nameEn = "Maghrib",
                time = maghrib,
                isAthanEnabled = athanEnabledProvider?.invoke("maghrib") ?: true,
                muadhinId = muadhinProvider?.invoke("maghrib") ?: "athan_aqsa"
            ),
            PrayerTime(
                id = "isha",
                nameAr = "العشاء",
                nameEn = "Isha",
                time = isha,
                isAthanEnabled = athanEnabledProvider?.invoke("isha") ?: true,
                muadhinId = muadhinProvider?.invoke("isha") ?: "athan_cairo"
            )
        )

        // Find which prayer is next
        var nextFound = false
        return rawList.map { prayer ->
            if (!nextFound && prayer.time.isAfter(now)) {
                nextFound = true
                prayer.copy(isNext = true)
            } else {
                prayer
            }
        }.let { list ->
            if (!nextFound && list.isNotEmpty()) {
                list.mapIndexed { index, item ->
                    if (index == 0) item.copy(isNext = true) else item
                }
            } else {
                list
            }
        }
    }

    // Overload for city ID
    fun calculatePrayers(cityId: String = "makkah", is24Hour: Boolean = false): List<PrayerTime> {
        val coords = cityCoordinates[cityId] ?: Pair(21.4225, 39.8262)
        return calculatePrayers(coords.first, coords.second, is24Hour)
    }
}
