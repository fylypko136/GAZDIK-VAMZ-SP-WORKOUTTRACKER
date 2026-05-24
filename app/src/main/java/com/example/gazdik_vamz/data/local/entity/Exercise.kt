package com.example.gazdik_vamz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Databázová entita reprezentujúca jeden cvik.
 *
 * Cviky sú uložené nezávisle od rutín — jeden cvik môže byť v niekoľkých rutinách
 * Vymazanie rutiny neodstráni cviky; vymazanie cviku taktiež neodstráni históriu sérií
 */
    // https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "",
    val notes: String = ""
)
