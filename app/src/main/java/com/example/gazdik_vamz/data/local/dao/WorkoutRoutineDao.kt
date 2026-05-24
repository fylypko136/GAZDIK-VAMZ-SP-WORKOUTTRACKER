package com.example.gazdik_vamz.data.local.dao

import androidx.room.*
import com.example.gazdik_vamz.data.local.entity.RoutineExerciseCrossRef
import com.example.gazdik_vamz.data.local.entity.WorkoutRoutine
import kotlinx.coroutines.flow.Flow

/** Data Access Objects pre CREATE, READ, UPDATE a DELETE pre tabuľku WorkoutRoutine */
// https://www.youtube.com/watch?v=bOd3wO0uFr8 - The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android - Philipp Lackner
@Dao
interface WorkoutRoutineDao {

    /** Vrátenie všetkých rutín zoradených od najnovšej po najstaršiu (podľa createdAt). */
    @Query("SELECT * FROM workout_routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<WorkoutRoutine>>

    /** Vráti rutinu s daným [id], alebo null ak neexistuje. Jednorazový suspend dotaz. */
    @Query("SELECT * FROM workout_routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): WorkoutRoutine?

    /** Vloženie novej rutiny a vrátenie jej ID-čka. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutine): Long

    /** Aktualizovanie existujúcej rutiny (zmena názvu alebo popisu). */
    @Update
    suspend fun updateRoutine(routine: WorkoutRoutine)

    /** Vymazanie rutiny */
    @Delete
    suspend fun deleteRoutine(routine: WorkoutRoutine)

    /** Pridanie prepojenia cviku s rutinou */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: RoutineExerciseCrossRef)

    /** Odstránenie prepojenia cviku s rutinou */
    @Query("DELETE FROM routine_exercise_cross_ref WHERE routineId = :routineId AND exerciseId = :exerciseId")
    suspend fun deleteCrossRef(routineId: Long, exerciseId: Long)

    /** Vymazanie všetkých prepojení cviku pre danú rutinu. */
    @Query("DELETE FROM routine_exercise_cross_ref WHERE routineId = :routineId")
    suspend fun deleteAllCrossRefsForRoutine(routineId: Long)

    /** Aktualizovanie poradia cviku (šípka hore a dole v rutine) */
    @Query("UPDATE routine_exercise_cross_ref SET orderIndex = :newIndex WHERE routineId = :routineId AND exerciseId = :exerciseId")
    suspend fun updateExerciseOrder(routineId: Long, exerciseId: Long, newIndex: Int)
}
