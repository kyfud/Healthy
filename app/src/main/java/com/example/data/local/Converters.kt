package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority

class Converters {
    @TypeConverter
    fun fromPriority(priority: TaskPriority?): String? {
        return priority?.name
    }

    @TypeConverter
    fun toPriority(value: String?): TaskPriority? {
        return value?.let {
            try {
                TaskPriority.valueOf(it)
            } catch (e: Exception) {
                TaskPriority.MEDIUM
            }
        }
    }

    @TypeConverter
    fun fromCategory(category: TaskCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toCategory(value: String?): TaskCategory? {
        return value?.let {
            try {
                TaskCategory.valueOf(it)
            } catch (e: Exception) {
                TaskCategory.OTHER
            }
        }
    }
}
