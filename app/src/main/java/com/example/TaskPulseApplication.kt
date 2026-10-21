package com.example

import android.app.Application
import com.example.data.local.TaskPulseDatabase
import com.example.data.repository.TaskPulseRepository
import com.example.reminder.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TaskPulseApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var repository: TaskPulseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = TaskPulseDatabase.getDatabase(this)
        repository = TaskPulseRepository(database.taskDao(), database.habitDao())

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // Seed initial data asynchronously on first run
        applicationScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }
}
