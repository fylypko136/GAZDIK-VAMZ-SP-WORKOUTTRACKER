package com.example.gazdik_vamz.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.gazdik_vamz.data.local.AppDatabase
import com.example.gazdik_vamz.data.local.entity.*
import kotlinx.coroutines.flow.Flow


class WorkoutRepository(db: AppDatabase) {

    private val routineDao = db.workoutRoutineDao()
    private val exerciseDao = db.exerciseDao()
    private val sessionDao = db.workoutSessionDao()
    private val setDao = db.exerciseSetDao()


    /** Vrátenie všetkých rutín zoradených od najstaršej. */
    fun getAllRoutines(): Flow<List<WorkoutRoutine>> = routineDao.getAllRoutines()

    /** Načítanie rutiny podľa ID. */
    suspend fun getRoutineById(id: Long): WorkoutRoutine? = routineDao.getRoutineById(id)

    /** Vloženie novej rutiny */
    suspend fun insertRoutine(routine: WorkoutRoutine): Long = routineDao.insertRoutine(routine)

    /** Aktualizovanie existujúcej rutiny */
    suspend fun updateRoutine(routine: WorkoutRoutine) = routineDao.updateRoutine(routine)

    /** Vymazanie rutiny so všetkými záznamami. */
    suspend fun deleteRoutine(routine: WorkoutRoutine) {
        routineDao.deleteAllCrossRefsForRoutine(routine.id)
        routineDao.deleteRoutine(routine)
    }

    /** Zobrazenie všetkých cvikoch abecedne. */
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    /** Zobrazenie cvikov priradených k routineId. */
    fun getExercisesForRoutine(routineId: Long): Flow<List<Exercise>> =
        exerciseDao.getExercisesForRoutine(routineId)

    /** Vloženie nového cviku. */
    suspend fun insertExercise(exercise: Exercise): Long = exerciseDao.insertExercise(exercise)

    /** Vymazanie cviku. */
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.deleteExercise(exercise)

    /** Vytvorenie väzby medzi rutinou a cvikom na pozícii orderIndex. */
    suspend fun addExerciseToRoutine(routineId: Long, exerciseId: Long, orderIndex: Int) {
        routineDao.insertCrossRef(RoutineExerciseCrossRef(routineId, exerciseId, orderIndex))
    }

    /** Odstránenue väzby medzi rutinou a cvikom. */
    suspend fun removeExerciseFromRoutine(routineId: Long, exerciseId: Long) {
        routineDao.deleteCrossRef(routineId, exerciseId)
    }

    /** Aktualizovanie poradie cviku v rutine. */
    suspend fun updateExerciseOrder(routineId: Long, exerciseId: Long, newIndex: Int) {
        routineDao.updateExerciseOrder(routineId, exerciseId, newIndex)
    }

    /** Zobrazenie všetkých sessions od najnovšej pre StatsScreen. */
    fun getAllSessions(): Flow<List<WorkoutSession>> = sessionDao.getAllSessions()

    /** Načitanie sessiony pomocou ID. */
    suspend fun getSessionById(id: Long): WorkoutSession? = sessionDao.getSessionById(id)

    /** Vloženie novej session-y. */
    suspend fun insertSession(session: WorkoutSession): Long = sessionDao.insertSession(session)

    /** Aktualizivanue session-y. */
    suspend fun updateSession(session: WorkoutSession) = sessionDao.updateSession(session)

    /** Vymazanie session-y spolu so všetkými jej sériami. */
    suspend fun deleteSession(session: WorkoutSession) {
        setDao.deleteSetsForSession(session.id)
        sessionDao.deleteSession(session)
    }

    /** Vrátenie celkového počtu session-s. */
    fun getTotalSessionCount(): Flow<Int> = sessionDao.getTotalSessionCount()

    /** Vrátenie session-s od zadaného času. Na výpočet tento týžden a tento mesiac. */
    fun getSessionsSince(since: Long): Flow<List<WorkoutSession>> = sessionDao.getSessionsSince(since)

    /** Vratenie SessionSummary pre HistoryScreen. */
    fun getPagedSessionSummaries(): Flow<PagingData<SessionSummary>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { sessionDao.getPagedSessionSummaries() }
    ).flow

    /** Načítanie počtu sessions od (since) pre WorkoutReminderWorker. */
    suspend fun getSessionCountSince(since: Long): Int = sessionDao.getSessionCountSince(since)

    /** Vrátenie startTime a setCount pre graf počtu sérií na StatsScreen. */
    fun getSessionSetCounts(): Flow<List<SessionSetCount>> = sessionDao.getSessionSetCounts()

    /** Vrátenie setov v aktuálnej sérií */
    fun getSetsForSession(sessionId: Long): Flow<List<ExerciseSet>> =
        setDao.getSetsForSession(sessionId)

    /** Vrátenie posledných sérií pre exerciseId */
    fun getRecentSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>> =
        setDao.getRecentSetsForExercise(exerciseId)

    /** Vloženie série */
    suspend fun insertSet(set: ExerciseSet): Long = setDao.insertSet(set)

    /** Vymazanie série. */
    suspend fun deleteSet(set: ExerciseSet) = setDao.deleteSet(set)

    /** Vrátenie počtu sérií v aktuálnej session-e. */
    suspend fun getSetCountForSession(sessionId: Long): Int =
        setDao.getSetCountForSession(sessionId)

    /** Vrátenie série z predchádzajúcej session pre exerciuseID, okrem aktuálnej session-y */
    suspend fun getLastSessionSets(exerciseId: Long, currentSessionId: Long): List<ExerciseSet> =
        setDao.getLastSessionSets(exerciseId, currentSessionId)

    /** Načítanie všetkých sérií session.*/
    suspend fun getSetsForSessionOnce(sessionId: Long): List<ExerciseSet> =
        setDao.getSetsForSessionOnce(sessionId)

    /** Vrátenie cviku podľa ID, ak neexistuje vráti null. */
    suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getExerciseById(id)

    /** Vrátenie maximálnej váhy za session pre exerciseID (posledných 10 sessions). */
    suspend fun getWeightProgressionForExercise(exerciseId: Long): List<WeightProgressEntry> =
        setDao.getWeightProgressionForExercise(exerciseId)
}
