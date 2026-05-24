package com.example.gazdik_vamz.data.local.dao

import androidx.room.*
import com.example.gazdik_vamz.data.local.entity.ExerciseSet
import com.example.gazdik_vamz.data.local.entity.WeightProgressEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Objects pre CREATE, READ, UPDATE a DELETE pre tabuľku exercise_sets.
   Zobrazenie sérií, hodnoty z predchádzajúcej session, graf progresu váhy.
 */
// https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner
@Dao
interface ExerciseSetDao {

    /** Vrátenie všetkých sérií v danej session zoradené podľa názvu cviku a čísla série. */
    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY exerciseName, setNumber")
    fun getSetsForSession(sessionId: Long): Flow<List<ExerciseSet>>

    /** Vrátenie série pre daný cvik naprieč všetkými sessions, od najnovšej. */
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId ORDER BY sessionId DESC")
    fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>>

    /** Vloženie série a jej ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: ExerciseSet): Long

    /** Vymazanie jednej série z tréningu */
    @Delete
    suspend fun deleteSet(set: ExerciseSet)

    /** Vymazanie všetkých sérií priradených ku danej sessione. */
    @Query("DELETE FROM exercise_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Long)

    /** Vrátenie celkového počtu sérií v sessione. */
    @Query("SELECT COUNT(*) FROM exercise_sets WHERE sessionId = :sessionId")
    suspend fun getSetCountForSession(sessionId: Long): Int

    /**
     * Vrátenie posledného limitu na cviku.
     * Predvolený limit je 10
     */
    @Query("""
        SELECT * FROM exercise_sets
        WHERE exerciseId = :exerciseId
        ORDER BY sessionId DESC
        LIMIT :limit
    """)
    fun getRecentSetsForExercise(exerciseId: Long, limit: Int = 10): Flow<List<ExerciseSet>>

    /** Vrátenie série z poslednej sessiony pre daný cvik. */
    @Query("""
        SELECT * FROM exercise_sets
        WHERE exerciseId = :exerciseId
          AND sessionId != :currentSessionId
          AND sessionId = (
              SELECT MAX(sessionId) FROM exercise_sets
              WHERE exerciseId = :exerciseId AND sessionId != :currentSessionId
          )
        ORDER BY setNumber
    """)
    suspend fun getLastSessionSets(exerciseId: Long, currentSessionId: Long): List<ExerciseSet>

    /** Vrátenie všetkých sérií v sessione pre zobrazenie v detaile. */
    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY exerciseName, setNumber")
    suspend fun getSetsForSessionOnce(sessionId: Long): List<ExerciseSet>

    /**
     * Vrátenie maximálnej zdvihnutej váhy za session pre daný cvik (posledných 10 sessions, od najstaršej).
     * Výsledok je vykreslený ako stĺpcový graf.
     */
    @Query("""
        SELECT es.sessionId, MAX(es.weightKg) AS maxWeight, ws.startTime
        FROM exercise_sets es
        JOIN workout_sessions ws ON es.sessionId = ws.id
        WHERE es.exerciseId = :exerciseId
        GROUP BY es.sessionId
        ORDER BY ws.startTime ASC
        LIMIT 10
    """)
    suspend fun getWeightProgressionForExercise(exerciseId: Long): List<WeightProgressEntry>
}
