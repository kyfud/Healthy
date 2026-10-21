package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.ReminderReceiver

object NotificationHelper {
    const val CHANNEL_TASKS_ID = "taskpulse_tasks_channel"
    const val CHANNEL_HABITS_ID = "taskpulse_habits_channel"
    private const val CHANNEL_TASKS_NAME = "Task Reminders"
    private const val CHANNEL_HABITS_NAME = "Habit Reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val taskChannel = NotificationChannel(
                CHANNEL_TASKS_ID,
                CHANNEL_TASKS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for high priority and scheduled tasks"
                enableVibration(true)
            }

            val habitChannel = NotificationChannel(
                CHANNEL_HABITS_ID,
                CHANNEL_HABITS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily habit streak reminders and check-in alerts"
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(taskChannel)
            notificationManager.createNotificationChannel(habitChannel)
        }
    }

    fun showImmediateNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt(),
        channelId: String = CHANNEL_TASKS_ID
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet on Android 13+
        }
    }

    fun scheduleReminder(
        context: Context,
        title: String,
        message: String,
        triggerAtMillis: Long,
        notificationId: Int,
        channelId: String = CHANNEL_TASKS_ID
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_MESSAGE, message)
            putExtra(ReminderReceiver.EXTRA_ID, notificationId)
            putExtra(ReminderReceiver.EXTRA_CHANNEL_ID, channelId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            // Fallback: if scheduling exact alarm fails, show immediate reminder or handle gracefully
        }
    }

    fun cancelReminder(context: Context, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
