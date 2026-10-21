package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryWork
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium

enum class TaskPriority(val displayName: String, val level: Int) {
    HIGH("High", 3),
    MEDIUM("Medium", 2),
    LOW("Low", 1);

    fun getColor(): Color = when (this) {
        HIGH -> PriorityHigh
        MEDIUM -> PriorityMedium
        LOW -> PriorityLow
    }
}

enum class TaskCategory(val displayName: String) {
    WORK("Work"),
    PERSONAL("Personal"),
    HEALTH("Health"),
    STUDY("Study"),
    OTHER("Other");

    fun getColor(): Color = when (this) {
        WORK -> CategoryWork
        PERSONAL -> CategoryPersonal
        HEALTH -> CategoryHealth
        STUDY -> CategoryStudy
        OTHER -> CategoryOther
    }
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val dueDate: Long? = null, // epoch day timestamp
    val dueTime: String? = null, // e.g. "14:30"
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = false
)
