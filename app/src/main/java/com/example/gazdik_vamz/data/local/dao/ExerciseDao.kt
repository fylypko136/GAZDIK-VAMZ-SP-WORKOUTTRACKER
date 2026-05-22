package com.example.gazdik_vamz.data.local.dao

import androidx.room.*
import com.example.gazdik_vamz.data.local.entity.Exercise
import kotlinx.coroutines.flow.Flow

/** Data Access Objects pre CREATE, READ, UPDATE a DELETE. */
@Dao
interface ExerciseDao {

    /** Vrátenie všetkých cvikov abecedne podľa názvu. */
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    /** Vrátenie cvikov priradenému k rutine. */
    @Query("""
        SELECT e.* FROM exercises e
        INNER JOIN routine_exercise_cross_ref r ON e.id = r.exerciseId
        WHERE r.routineId = :routineId
        ORDER BY r.orderIndex ASC
    """)
    fun getExercisesForRoutine(routineId: Long): Flow<List<Exercise>>

    /** Vrátenie cviku podľa ID, alebo null ak neexistuje. */
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): Exercise?

    /** Vloženie cviku s návratovou hodnotou ID, ak existuje rovnaky PK prepíše sa. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    /** Aktualizácia existujúceho cviku */
    @Update
    suspend fun updateExercise(exercise: Exercise)

    /** Vymazanie cviku z databázy */
    @Delete
    suspend fun deleteExercise(exercise: Exercise)
}
