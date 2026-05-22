package com.example.gazdik_vamz

import android.content.Context
import androidx.core.content.edit


class ActiveSessionPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("active_session_prefs", Context.MODE_PRIVATE)


    fun save(sessionId: Long, startTime: Long) {
        prefs.edit {
            putLong(KEY_SESSION_ID, sessionId)
            putLong(KEY_START_TIME, startTime)
        }
    }

    fun getSessionId(): Long = prefs.getLong(KEY_SESSION_ID, -1L)

    fun getStartTime(): Long = prefs.getLong(KEY_START_TIME, 0L)


    fun clear() = prefs.edit { clear() }


    fun hasValidSession(): Boolean {
        val id = getSessionId()
        if (id == -1L) return false
        val elapsed = System.currentTimeMillis() - getStartTime()
        return elapsed < FIVE_HOURS_MS
    }

    companion object {
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_START_TIME = "start_time"
        private const val FIVE_HOURS_MS = 5 * 60 * 60 * 1000L
    }
}
