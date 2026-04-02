package com.yep.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.yep.app.R
import com.yep.app.worker.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_ID = "yep_reminders"
    const val WORK_TAG = "yep_daily_reminder"
    const val NOTIFICATION_ID = 1001

    private val MESSAGES = listOf(
        "Quick check before you head out?",
        "Tap your morning list — takes 5 seconds.",
        "Your future self will thank you."
    )

    fun createChannel(context: Context) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Gentle daily reminder to check your list" }
            mgr.createNotificationChannel(channel)
        }
    }

    fun showReminder(context: Context) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val index = (System.currentTimeMillis() / 86_400_000L % MESSAGES.size).toInt()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Yep.")
            .setContentText(MESSAGES[index])
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        mgr.notify(NOTIFICATION_ID, notification)
    }

    fun scheduleReminder(context: Context, timeString: String) {
        val parts = timeString.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        val initialDelay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
}
