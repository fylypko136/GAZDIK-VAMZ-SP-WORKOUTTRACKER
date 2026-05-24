package com.example.gazdik_vamz.data.local.entity

import androidx.room.Entity

/**
 * Prepojovacia tabuľka medzi Exercise a WorkoutRoutine.
 * Každý cvik sa môže objaviť v danej rutine len raz.
 */
// https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner

@Entity(
    tableName = "routine_exercise_cross_ref",
    primaryKeys = ["routineId", "exerciseId"]
)
data class RoutineExerciseCrossRef(
    val routineId: Long,  // FK na WorkoutRoutine.id
    val exerciseId: Long, // FK na Exercise.id
    val orderIndex: Int = 0 // Poradie cviku v rutine (0 = prvý)
)
