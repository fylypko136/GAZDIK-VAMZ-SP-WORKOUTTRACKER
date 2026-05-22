package com.example.gazdik_vamz.data.local.entity

import androidx.room.Entity

/**
 * Prepojovacia tabuľka medzi Exercise a WorkoutRoutine.
 * Každý cvik sa môže objaviť v danej rutine len raz.
 */

@Entity(
    tableName = "routine_exercise_cross_ref",
    primaryKeys = ["routineId", "exerciseId"]
)
data class RoutineExerciseCrossRef(
    val routineId: Long,  // FK na WorkoutRoutine.id
    val exerciseId: Long, // FK na Exercise.id
    val orderIndex: Int = 0 // Poradie cviku v rutine (0 = prvý)
)
