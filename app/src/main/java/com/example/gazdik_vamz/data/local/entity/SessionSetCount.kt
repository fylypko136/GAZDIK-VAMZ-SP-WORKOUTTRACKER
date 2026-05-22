package com.example.gazdik_vamz.data.local.entity

import androidx.room.ColumnInfo

/**
 * Párovanie session-y s celkovým počtom sérií.
 * Používa sa pre graf "Počet sérií" na StatsScreen.
 */

data class SessionSetCount(
    @ColumnInfo(name = "startTime") val startTime: Long,
    @ColumnInfo(name = "setCount") val setCount: Int
)
