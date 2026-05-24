package com.example.gazdik_vamz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Reprezentovanie jednej tréningovej session-y. */
// https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val routineId: Long? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationSeconds: Long? = null,
    val notes: String = ""
)
