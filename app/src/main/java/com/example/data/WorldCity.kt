package com.example.data

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WorldCity(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val zoneId: String,
    val isDefault: Boolean = false
) {
    fun getCurrentZonedDateTime(): ZonedDateTime {
        return try {
            ZonedDateTime.now(ZoneId.of(zoneId))
        } catch (e: Exception) {
            ZonedDateTime.now()
        }
    }

    fun getTimeFormatted(is24Hour: Boolean = false): String {
        val zdt = getCurrentZonedDateTime()
        val pattern = if (is24Hour) "HH:mm" else "hh:mm"
        return zdt.format(DateTimeFormatter.ofPattern(pattern, Locale.US))
    }

    fun getAmPmArabic(): String {
        val zdt = getCurrentZonedDateTime()
        return if (zdt.hour < 12) "ص" else "م"
    }

    fun getTimeDifferenceArabic(localZoneId: ZoneId = ZoneId.systemDefault()): String {
        val localOffset = localZoneId.rules.getOffset(Instant.now()).totalSeconds
        val targetOffset = try {
            ZoneId.of(zoneId).rules.getOffset(Instant.now()).totalSeconds
        } catch (e: Exception) {
            localOffset
        }
        val diffHours = (targetOffset - localOffset) / 3600
        val diffMinutes = Math.abs((targetOffset - localOffset) % 3600) / 60

        return when {
            diffHours == 0 && diffMinutes == 0 -> "نفس التوقيت المحلي"
            diffHours > 0 -> {
                if (diffMinutes > 0) "+$diffHours س و $diffMinutes د" else "+$diffHours ساعة"
            }
            else -> {
                val absHours = Math.abs(diffHours)
                if (diffMinutes > 0) "-$absHours س و $diffMinutes د" else "-$absHours ساعة"
            }
        }
    }

    fun isDaytime(): Boolean {
        val hour = getCurrentZonedDateTime().hour
        return hour in 6..18
    }
}

object DefaultWorldCities {
    val allCities = listOf(
        WorldCity("makkah", "مكة المكرمة", "Makkah", "المملكة العربية السعودية", "Asia/Riyadh", true),
        WorldCity("jerusalem", "القدس الشريف", "Jerusalem", "فلسطين", "Asia/Jerusalem", true),
        WorldCity("cairo", "القاهرة", "Cairo", "مصر", "Africa/Cairo", true),
        WorldCity("dubai", "دبي", "Dubai", "الإمارات العربية المتحدة", "Asia/Dubai", true),
        WorldCity("riyadh", "الرياض", "Riyadh", "المملكة العربية السعودية", "Asia/Riyadh", false),
        WorldCity("doha", "الدوحة", "Doha", "قطر", "Asia/Qatar", false),
        WorldCity("kuwait", "الكويت", "Kuwait City", "الكويت", "Asia/Kuwait", false),
        WorldCity("amman", "عمّان", "Amman", "الأردن", "Asia/Amman", false),
        WorldCity("baghdad", "بغداد", "Baghdad", "العراق", "Asia/Baghdad", false),
        WorldCity("beirut", "بيروت", "Beirut", "لبنان", "Asia/Beirut", false),
        WorldCity("damascus", "دمشق", "Damascus", "سوريا", "Asia/Damascus", false),
        WorldCity("muscat", "مسقط", "Muscat", "عُمان", "Asia/Muscat", false),
        WorldCity("manama", "المنامة", "Manama", "البحرين", "Asia/Bahrain", false),
        WorldCity("tunis", "تونس", "Tunis", "تونس", "Africa/Tunis", false),
        WorldCity("algiers", "الجزائر", "Algiers", "الجزائر", "Africa/Algiers", false),
        WorldCity("rabat", "الرباط", "Rabat", "المغرب", "Africa/Casablanca", false),
        WorldCity("tripoli", "طرابلس", "Tripoli", "ليبيا", "Africa/Tripoli", false),
        WorldCity("khartoum", "الخرطوم", "Khartoum", "السودان", "Africa/Khartoum", false),
        WorldCity("london", "لندن", "London", "المملكة المتحدة", "Europe/London", true),
        WorldCity("paris", "باريس", "Paris", "فرنسا", "Europe/Paris", false),
        WorldCity("istanbul", "إسطنبول", "Istanbul", "تركيا", "Europe/Istanbul", true),
        WorldCity("newyork", "نيويورك", "New York", "الولايات المتحدة", "America/New_York", true),
        WorldCity("tokyo", "طوكيو", "Tokyo", "اليابان", "Asia/Tokyo", true),
        WorldCity("sydney", "سيدني", "Sydney", "أستراليا", "Australia/Sydney", false),
        WorldCity("singapore", "سنغافورة", "Singapore", "سنغافورة", "Asia/Singapore", false),
        WorldCity("berlin", "برلين", "Berlin", "ألمانيا", "Europe/Berlin", false)
    )
}
