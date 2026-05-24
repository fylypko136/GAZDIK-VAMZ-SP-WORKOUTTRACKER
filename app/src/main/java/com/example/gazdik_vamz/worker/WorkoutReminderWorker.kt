package com.example.gazdik_vamz.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gazdik_vamz.MainActivity
import com.example.gazdik_vamz.R
import com.example.gazdik_vamz.data.local.AppDatabase
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import com.example.gazdik_vamz.widget.WorkoutWidget
import java.util.concurrent.TimeUnit

// https://www.youtube.com/watch?v=LP623htmWcI - Local Notifications in Android - The Full Guide (Android Studio Tutorial) - Philipp Lackner
// https://www.youtube.com/watch?v=A2JetouoNSc - WorkManager - Android Basics 2023 - Philipp Lackner

class WorkoutReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result {
        val repository = WorkoutRepository(AppDatabase.getInstance(context))
        val threeDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
        val recentCount = repository.getSessionCountSince(threeDaysAgo)

        WorkoutWidget.updateAll(context)

        if (recentCount == 0) {
            sendReminderNotification()
        }

        return Result.success()
    }

    private fun sendReminderNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    context.getString(R.string.notif_reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = context.getString(R.string.notif_reminder_channel_desc) }
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notif_reminder_title))
            .setContentText(context.getString(R.string.notif_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Notifikácia zmizne po kliknutí
            .build()

        manager.notify(REMINDER_NOTIFICATION_ID, notification)
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "workout_reminder_channel"
        private const val REMINDER_NOTIFICATION_ID = 42
        const val WORK_NAME = "workout_reminder"
    }
}
