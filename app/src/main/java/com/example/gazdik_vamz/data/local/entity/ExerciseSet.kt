package com.example.gazdik_vamz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Databázová entita reprezentujúca jednu sériu v tréningu.
 *
 * Každá séria patrí do konkrétnej session a konkrétneho cviku.
 */
// https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner
@Entity(tableName = "exercise_sets")
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float
)
