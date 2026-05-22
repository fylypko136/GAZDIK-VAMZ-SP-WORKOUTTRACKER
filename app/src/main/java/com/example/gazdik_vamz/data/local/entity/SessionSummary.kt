package com.example.gazdik_vamz.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/** Výsledok dotazu kombinujúci WorkoutSession s počtom jej sérií. */
data class SessionSummary(
    @Embedded val session: WorkoutSession,
    @ColumnInfo(name = "setCount") val setCount: Int = 0
)
