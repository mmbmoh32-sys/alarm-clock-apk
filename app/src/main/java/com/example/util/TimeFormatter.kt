package com.example.util

import com.example.data.Alarm
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

object TimeFormatter {

    fun formatTime(hour: Int, minute: Int, is24Hour: Boolean): Pair<String, String> {
        val timeStr: String
        val amPmStr: String

        if (is24Hour) {
            timeStr = String.format(Locale.US, "%02d:%02d", hour, minute)
            amPmStr = ""
        } else {
            val h = when (hour) {
                0 -> 12
                in 1..12 -> hour
                else -> hour - 12
            }
            timeStr = String.format(Locale.US, "%02d:%02d", h, minute)
            amPmStr = if (hour < 12) "ص" else "م"
        }
        return Pair(timeStr, amPmStr)
    }

    fun getRemainingTimeDescription(alarms: List<Alarm>): String? {
        val activeAlarms = alarms.filter { it.isEnabled }
        if (activeAlarms.isEmpty()) return null

        val now = LocalDateTime.now()
        var minMinutesUntil = Long.MAX_VALUE

        for (alarm in activeAlarms) {
            val minutes = getMinutesUntilNextTrigger(now, alarm)
            if (minutes in 0 until minMinutesUntil) {
                minMinutesUntil = minutes
            }
        }

        if (minMinutesUntil == Long.MAX_VALUE) return null

        val hours = minMinutesUntil / 60
        val remainingMinutes = minMinutesUntil % 60

        return when {
            hours == 0L && remainingMinutes <= 1L -> "يرن بعد أقل من دقيقة"
            hours == 0L -> "يرن بعد $remainingMinutes دقيقة"
            hours == 1L && remainingMinutes == 0L -> "يرن بعد ساعة واحدة"
            hours == 1L -> "يرن بعد ساعة و $remainingMinutes دقيقة"
            hours == 2L && remainingMinutes == 0L -> "يرن بعد ساعتين"
            hours == 2L -> "يرن بعد ساعتين و $remainingMinutes دقيقة"
            remainingMinutes == 0L -> "يرن بعد $hours ساعات"
            else -> "يرن بعد $hours ساعات و $remainingMinutes دقيقة"
        }
    }

    private fun getMinutesUntilNextTrigger(now: LocalDateTime, alarm: Alarm): Long {
        val days = alarm.getDaysList()

        if (days.isEmpty()) {
            // One-time alarm
            var target = now.withHour(alarm.hour).withMinute(alarm.minute).withSecond(0).withNano(0)
            if (target.isBefore(now) || target.isEqual(now)) {
                target = target.plusDays(1)
            }
            return ChronoUnit.MINUTES.between(now, target)
        } else {
            // Repeating alarm on specific days of week
            // Note: In our model 1=Sunday, 2=Monday, 3=Tuesday, 4=Wednesday, 5=Thursday, 6=Friday, 7=Saturday
            // Java DayOfWeek: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
            for (dayOffset in 0..7) {
                val candidateDate = now.plusDays(dayOffset.toLong())
                val javaDay = candidateDate.dayOfWeek
                val modelDay = when (javaDay) {
                    DayOfWeek.SUNDAY -> 1
                    DayOfWeek.MONDAY -> 2
                    DayOfWeek.TUESDAY -> 3
                    DayOfWeek.WEDNESDAY -> 4
                    DayOfWeek.THURSDAY -> 5
                    DayOfWeek.FRIDAY -> 6
                    DayOfWeek.SATURDAY -> 7
                }

                if (days.contains(modelDay)) {
                    val candidateDateTime = candidateDate.withHour(alarm.hour).withMinute(alarm.minute).withSecond(0).withNano(0)
                    if (candidateDateTime.isAfter(now)) {
                        return ChronoUnit.MINUTES.between(now, candidateDateTime)
                    }
                }
            }
            return ChronoUnit.MINUTES.between(now, now.plusDays(1))
        }
    }

    fun formatStopwatch(elapsedMillis: Long): String {
        val minutes = (elapsedMillis / 60000) % 60
        val seconds = (elapsedMillis / 1000) % 60
        val millis = (elapsedMillis % 1000) / 10
        return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, millis)
    }

    fun calculateSleepDuration(bedHour: Int, bedMinute: Int, wakeHour: Int, wakeMinute: Int): Pair<Float, Int> {
        var bedTotalMinutes = bedHour * 60 + bedMinute
        var wakeTotalMinutes = wakeHour * 60 + wakeMinute
        if (wakeTotalMinutes <= bedTotalMinutes) {
            wakeTotalMinutes += 24 * 60
        }
        val diffMinutes = wakeTotalMinutes - bedTotalMinutes
        val hours = diffMinutes / 60f
        val cycles = diffMinutes / 90 // 90 min per standard REM/NREM sleep cycle
        return Pair(hours, cycles)
    }
}
