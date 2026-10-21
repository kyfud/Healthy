package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.HEALTH,
    val targetDaysPerWeek: Int = 7,
    val colorHex: String = "#4F46E5",
    val iconKey: String = "fitness", // fitness, water, book, meditation, code, walk
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val reminderTime: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
