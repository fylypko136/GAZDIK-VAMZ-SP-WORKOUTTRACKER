package com.example.gazdik_vamz.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.gazdik_vamz.data.local.entity.SessionSummary
import com.example.gazdik_vamz.data.local.entity.SessionSetCount
import com.example.gazdik_vamz.data.local.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow

/** Data Access Objects pre CREATE, READ, UPDATE a DELETE pre tabuľku workout_sessions. */
// https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner
// https://www.youtube.com/watch?v=NlzVx8q1YVg - The Ultimate Guide to Android Pagination with Paging 3 & Jetpack Compose and clean architecture - CodeWithSaid
@Dao
interface WorkoutSessionDao {

    /** Vrátenue všetkých tréningov zoradených od najnovšieho */
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    /** Vrátenie session-y s daným ID */
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSession?

    /** Vloženie novej session-y a vrátenie jej ID */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    /** Aktualizovanie prebiahajúcej session-y */
    @Update
    suspend fun updateSession(session: WorkoutSession)

    /** Vymazanie session-y z databázy. */
    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    /** Vrátenie celkového počtu tréningov.*/
    @Query("SELECT COUNT(*) FROM workout_sessions")
    fun getTotalSessionCount(): Flow<Int>

    /**
     * Vrátenie tréningov, ktoré začali po danom čase
     * Používa sa pre štatistiky.
     */
    @Query("SELECT * FROM workout_sessions WHERE startTime >= :since ORDER BY startTime DESC")
    fun getSessionsSince(since: Long): Flow<List<WorkoutSession>>

    /** Vrátenie PagingSource pre stránkovaný zoznam tréningov. */
    @Query("""
        SELECT ws.*, COUNT(es.id) AS setCount
        FROM workout_sessions ws
        LEFT JOIN exercise_sets es ON ws.id = es.sessionId
        GROUP BY ws.id
        ORDER BY ws.startTime DESC
    """)
    fun getPagedSessionSummaries(): PagingSource<Int, SessionSummary>

    /**
     * Vrétenie počtu tréningov od dátumu.
     * Používa sa vo WorkoutReminderWorker — ak = 0, pošle sa notifikácia s pripomienkou.
     */
    @Query("SELECT COUNT(*) FROM workout_sessions WHERE startTime >= :since")
    suspend fun getSessionCountSince(since: Long): Int

    /**
     * Vrátenie párov (startTime, setCount) pre každý tréning.
     * Dáta sa vykresľujú ako stĺpcový graf "počet sérií" na StatsScreen.
     */
    @Query("""
        SELECT ws.startTime, COUNT(es.id) AS setCount
        FROM workout_sessions ws
        LEFT JOIN exercise_sets es ON ws.id = es.sessionId
        GROUP BY ws.id
        ORDER BY ws.startTime DESC
    """)
    fun getSessionSetCounts(): Flow<List<SessionSetCount>>
}
