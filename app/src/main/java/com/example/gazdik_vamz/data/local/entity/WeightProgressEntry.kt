package com.example.gazdik_vamz.data.local.entity

import androidx.room.ColumnInfo

/**
 * Vykreslovanie grafu progresu váhy konkrétneho cviku.
 * Pre každú session-u vráti maximálnu váhu použitú pri danom cviku.
 */
data class WeightProgressEntry(
    @ColumnInfo(name = "sessionId") val sessionId: Long,
    @ColumnInfo(name = "maxWeight") val maxWeight: Float,
    @ColumnInfo(name = "startTime") val startTime: Long
)
