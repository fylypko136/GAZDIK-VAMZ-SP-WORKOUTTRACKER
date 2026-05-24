package com.example.gazdik_vamz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Reprezentovanie tréningovej rutiny */
// https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner

@Entity(tableName = "workout_routines")
data class WorkoutRoutine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
