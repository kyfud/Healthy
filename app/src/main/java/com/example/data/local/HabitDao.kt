package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // Habit Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLog): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteHabitLog(habitId: Long, dateString: String)

    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateString DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateString DESC")
    suspend fun getLogsForHabitSync(habitId: Long): List<HabitLog>

    @Query("SELECT * FROM habit_logs ORDER BY dateString DESC")
    fun getAllLogs(): Flow<List<HabitLog>>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun isHabitLogged(habitId: Long, dateString: String): Int

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteAllLogsForHabit(habitId: Long)
}
