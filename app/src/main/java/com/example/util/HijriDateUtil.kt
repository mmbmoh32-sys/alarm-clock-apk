package com.example.util

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object HijriDateUtil {
    private val hijriMonthNames = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val dayNames = listOf(
        "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد"
    )

    private val gregorianMonthNames = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    fun getTodayHijriArabic(): String {
        return try {
            val hijrahDate = HijrahDate.now()
            val day = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            val month = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val year = hijrahDate.get(java.time.temporal.ChronoField.YEAR)

            val monthName = hijriMonthNames.getOrElse(month - 1) { "هجري" }
            "$day $monthName $year هـ"
        } catch (e: Exception) {
            "27 ربيع الآخر 1448 هـ"
        }
    }

    fun getTodayGregorianArabic(): String {
        val today = LocalDate.now()
        val dayOfWeekIndex = today.dayOfWeek.value - 1
        val dayName = dayNames.getOrElse(dayOfWeekIndex) { "" }
        val dayOfMonth = today.dayOfMonth
        val monthName = gregorianMonthNames.getOrElse(today.monthValue - 1) { "" }
        val year = today.year

        return "$dayName، $dayOfMonth $monthName $year م"
    }
}
