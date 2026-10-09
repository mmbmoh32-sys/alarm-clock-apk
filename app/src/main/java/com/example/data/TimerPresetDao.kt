package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerPresetDao {
    @Query("SELECT * FROM timer_presets ORDER BY durationSeconds ASC")
    fun getAllPresets(): Flow<List<TimerPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: TimerPreset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(presets: List<TimerPreset>)

    @Delete
    suspend fun deletePreset(preset: TimerPreset)

    @Query("SELECT COUNT(*) FROM timer_presets")
    suspend fun getCount(): Int
}
