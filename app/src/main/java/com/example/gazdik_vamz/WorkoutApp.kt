package com.example.gazdik_vamz

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gazdik_vamz.data.local.AppDatabase
import com.example.gazdik_vamz.data.local.entity.Exercise
import com.example.gazdik_vamz.data.local.entity.RoutineExerciseCrossRef
import com.example.gazdik_vamz.data.local.entity.WorkoutRoutine
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import com.example.gazdik_vamz.worker.WorkoutReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/** Aplikačná trieda */
// Pri tvorení semestrálnej práce bol použitý AI asistent (Claude) ako pomocný nástroj
// pri písaní KDoc komentárov a úprave čitateľnosti kódu.

//Pri písaní boli využité následovné tutoriály
// https://www.youtube.com/watch?v=LP623htmWcI - Local Notifications in Android - The Full Guide (Android Studio Tutorial) - Philipp Lackner
// https://www.youtube.com/watch?v=A2JetouoNSc - WorkManager - Android Basics 2023 - Philipp Lackner
class WorkoutApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { WorkoutRepository(database) }
    val activeSessionPrefs by lazy { ActiveSessionPrefs(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleWorkoutReminder()
        CoroutineScope(Dispatchers.IO).launch { ensureDefaults() }
    }


    private fun scheduleWorkoutReminder() {
        val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WorkoutReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }


    private suspend fun ensureDefaults() {
        val exerciseDao = database.exerciseDao()
        val routineDao  = database.workoutRoutineDao()

        val existingByName = exerciseDao.getAllExercises().first().associateBy { it.name }

        suspend fun ensureExercise(name: String, category: String): Long =
            existingByName[name]?.id
                ?: exerciseDao.insertExercise(Exercise(name = name, category = category))

        val benchId    = ensureExercise("Bench Press",           "Prsia")
        val inclineId  = ensureExercise("Incline Dumbbell Press","Prsia")
        val tricepsId  = ensureExercise("Triceps Pushdown",      "Triceps")
        val lateralId  = ensureExercise("Lateral Raises",        "Ramená")
        val latId      = ensureExercise("Lat Pullover",          "Chrbát")
        val rowId      = ensureExercise("Upper Back Row",        "Chrbát")
        val rearId     = ensureExercise("Rear Delt Flies",       "Ramená")
        val bicepsId   = ensureExercise("Biceps Curl",           "Biceps")
        val squatId    = ensureExercise("Squat",                 "Nohy")
        val legPressId = ensureExercise("Leg Press",             "Nohy")
        val legCurlId  = ensureExercise("Leg Curls",             "Nohy")
        val calfId     = ensureExercise("Calf Raises",           "Nohy")

        val existingRoutineNames = routineDao.getAllRoutines().first().map { it.name }.toSet()

        data class DefaultRoutine(val name: String, val description: String, val exerciseIds: List<Long>)

        listOf(
            DefaultRoutine("Push", "Prsia, Ramená, Triceps",           listOf(benchId, inclineId, tricepsId, lateralId)),
            DefaultRoutine("Pull", "Chrbát, Biceps, Zadné deltoidné",  listOf(latId, rowId, rearId, bicepsId)),
            DefaultRoutine("Legs", "Nohy a lýtka",                     listOf(squatId, legPressId, legCurlId, calfId))
        ).forEach { default ->
            if (default.name !in existingRoutineNames) {
                val routineId = routineDao.insertRoutine(
                    WorkoutRoutine(name = default.name, description = default.description)
                )
                default.exerciseIds.forEachIndexed { index, exId ->
                    routineDao.insertCrossRef(RoutineExerciseCrossRef(routineId, exId, index))
                }
            }
        }
    }


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    REST_TIMER_CHANNEL_ID,
                    getString(R.string.notif_rest_channel_name),
                    NotificationManager.IMPORTANCE_HIGH // HIGH = zvuk + notifikácia na zamknutej obrazovke
                ).apply { description = getString(R.string.notif_rest_channel_desc) }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    WORKOUT_DONE_CHANNEL_ID,
                    getString(R.string.notif_workout_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = getString(R.string.notif_workout_channel_desc) }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    WorkoutReminderWorker.REMINDER_CHANNEL_ID,
                    getString(R.string.notif_reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = getString(R.string.notif_reminder_channel_desc) }
            )
        }
    }

    companion object {
        const val REST_TIMER_CHANNEL_ID  = "rest_timer_channel"
        const val WORKOUT_DONE_CHANNEL_ID = "workout_done_channel"
    }
}
