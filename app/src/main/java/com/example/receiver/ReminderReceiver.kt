package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminder.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "TaskPulse Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Don't forget your scheduled task!"
        val notificationId = intent.getIntExtra(EXTRA_ID, System.currentTimeMillis().toInt())
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: NotificationHelper.CHANNEL_TASKS_ID

        NotificationHelper.showImmediateNotification(
            context = context,
            title = title,
            message = message,
            notificationId = notificationId,
            channelId = channelId
        )
    }
}
