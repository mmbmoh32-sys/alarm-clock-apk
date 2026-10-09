package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "منبه",
    val daysOfWeek: String = "", // Comma-separated: "1,2,3,4,5,6,7" or empty for once
    val snoozeMinutes: Int = 10,
    val maxSnoozeCount: Int = 3,
    val currentSnoozeCount: Int = 0,
    val soundId: String = "digital", // "digital", "radar", "dawn", "waves", "classic", "gentle"
    val isVibrate: Boolean = true,
    val isVolumeGradual: Boolean = true, // تدرج تصاعدي في الصوت
    val missionType: String = "NONE", // "NONE", "MATH", "SHAKE", "TYPING"
    val missionDifficulty: String = "MEDIUM" // "EASY", "MEDIUM", "HARD"
) {
    fun isRepeating(): Boolean = daysOfWeek.isNotBlank()

    fun getDaysList(): List<Int> {
        if (daysOfWeek.isBlank()) return emptyList()
        return daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    fun getFormattedDaysArabic(): String {
        val days = getDaysList()
        if (days.isEmpty()) return "مرة واحدة"
        if (days.size == 7) return "كل يوم"
        if (days.size == 5 && days.containsAll(listOf(1, 2, 3, 4, 5))) return "أيام العمل (الأحد - الخميس)"
        if (days.size == 2 && days.containsAll(listOf(6, 7))) return "عطلة نهاية الأسبوع (الجمعة والسبت)"
        
        val dayNames = mapOf(
            1 to "أحد",
            2 to "إثنين",
            3 to "ثلاثاء",
            4 to "أربعاء",
            5 to "خميس",
            6 to "جمعة",
            7 to "سبت"
        )
        return days.mapNotNull { dayNames[it] }.joinToString("، ")
    }

    fun getSoundDisplayName(): String {
        return when (soundId) {
            "digital" -> "رقمي كلاسيكي"
            "radar" -> "رادار سريع"
            "dawn" -> "أجراس الفجر"
            "waves" -> "أمواج هادئة"
            "classic" -> "جرس قديم"
            "gentle" -> "لحن الصباح"
            else -> "رقمي"
        }
    }

    fun getMissionDisplayName(): String {
        return when (missionType) {
            "MATH" -> "معادلة رياضية"
            "SHAKE" -> "هز الهاتف"
            "TYPING" -> "كتابة جملة"
            else -> "بدون مهمة"
        }
    }
}
