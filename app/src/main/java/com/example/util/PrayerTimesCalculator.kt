package com.example.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

data class PrayerTime(
    val nameAr: String,
    val nameEn: String,
    val time: LocalTime,
    val isNext: Boolean = false
) {
    fun getFormattedTime(is24Hour: Boolean = false): String {
        val pattern = if (is24Hour) "HH:mm" else "hh:mm"
        val timeStr = time.format(DateTimeFormatter.ofPattern(pattern, Locale.US))
        val amPm = if (time.hour < 12) "ص" else "م"
        return if (is24Hour) timeStr else "$timeStr $amPm"
    }
}

object PrayerTimesCalculator {
    // Coordinates for popular cities
    private val cityCoordinates = mapOf(
        "makkah" to Pair(21.4225, 39.8262),
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

    fun calculatePrayers(cityId: String = "makkah", is24Hour: Boolean = false): List<PrayerTime> {
        val coords = cityCoordinates[cityId] ?: Pair(21.4225, 39.8262) // Default Makkah
        val lat = coords.first
        val lng = coords.second

        val today = LocalDate.now()
        val dayOfYear = today.dayOfYear

        // Standard solar declination & equation of time approximation
        val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // Equation of time in minutes
        val declination = 23.45 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81))) // Degrees

        // Approximate timezone offset from longitude (15 deg per hour)
        val timeZoneOffset = (round(lng / 15.0)).toInt()

        // Solar noon in hours
        val solarNoon = 12.0 + (timeZoneOffset * 15.0 - lng) / 15.0 - (eot / 60.0)

        // Hour angle calculation for zenith
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

        // Asr angle (Shafi'i/standard shadow factor = 1)
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
            PrayerTime("الفجر", "Fajr", fajr),
            PrayerTime("الشروق", "Sunrise", sunrise),
            PrayerTime("الظهر", "Dhuhr", dhuhr),
            PrayerTime("العصر", "Asr", asr),
            PrayerTime("المغرب", "Maghrib", maghrib),
            PrayerTime("العشاء", "Isha", isha)
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
            // If none is after now, tomorrow Fajr is next
            if (!nextFound && list.isNotEmpty()) {
                list.mapIndexed { index, item ->
                    if (index == 0) item.copy(isNext = true) else item
                }
            } else {
                list
            }
        }
    }
}
