package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_presets")
data class TimerPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val durationSeconds: Int, // e.g. 180 for 3 min
    val iconName: String = "timer"
) {
    fun getFormattedDuration(): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60
        return when {
            hours > 0 -> String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
