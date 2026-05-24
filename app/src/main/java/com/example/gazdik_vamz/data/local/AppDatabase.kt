package com.example.gazdik_vamz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gazdik_vamz.data.local.dao.*
import com.example.gazdik_vamz.data.local.entity.*

/** Hlavná databáza aplikácie. */
// https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner
@Database(
    entities = [
        WorkoutRoutine::class,
        Exercise::class,
        RoutineExerciseCrossRef::class,
        WorkoutSession::class,
        ExerciseSet::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutRoutineDao(): WorkoutRoutineDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun exerciseSetDao(): ExerciseSetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
