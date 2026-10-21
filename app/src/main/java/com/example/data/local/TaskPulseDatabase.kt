package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.Task

@Database(
    entities = [Task::class, Habit::class, HabitLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskPulseDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: TaskPulseDatabase? = null

        fun getDatabase(context: Context): TaskPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskPulseDatabase::class.java,
                    "taskpulse_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
