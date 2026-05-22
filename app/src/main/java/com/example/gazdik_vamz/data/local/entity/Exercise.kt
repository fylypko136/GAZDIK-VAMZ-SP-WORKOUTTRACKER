package com.example.gazdik_vamz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Databázová entita reprezentujúca jeden cvik.
 *
 * Cviky sú uložené nezávisle od rutín — jeden cvik môže byť v niekoľkých rutinách
 * Vymazanie rutiny neodstráni cviky; vymazanie cviku taktiež neodstráni históriu sérií
 */

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "",
    val notes: String = ""
)
