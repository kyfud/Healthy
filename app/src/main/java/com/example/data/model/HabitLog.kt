package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "dateString"], unique = true)]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateString: String, // format: "yyyy-MM-dd"
    val completedAt: Long = System.currentTimeMillis()
)
