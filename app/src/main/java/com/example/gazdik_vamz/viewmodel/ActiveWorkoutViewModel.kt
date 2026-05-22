package com.example.gazdik_vamz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gazdik_vamz.ActiveSessionPrefs
import com.example.gazdik_vamz.data.local.entity.Exercise
import com.example.gazdik_vamz.data.local.entity.ExerciseSet
import com.example.gazdik_vamz.data.local.entity.WorkoutSession
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Stav jedného cviku počas aktívneho tréningu. */
data class ActiveExerciseState(
    val exercise: Exercise,
    val sets: List<ExerciseSet> = emptyList(),
    val lastSessionSets: List<ExerciseSet> = emptyList(),
    val progressMessage: String? = null
)

/** ViewModel pre obrazovku aktívneho tréningu. */
class ActiveWorkoutViewModel(
    private val repository: WorkoutRepository,
    private val prefs: ActiveSessionPrefs,
    private val routineId: Long,
    private val existingSessionId: Long
) : ViewModel() {

    private val _sessionId = MutableStateFlow<Long?>(null)
    val sessionId: StateFlow<Long?> = _sessionId.asStateFlow()

    private val _workoutName = MutableStateFlow("Tréning")
    val workoutName: StateFlow<String> = _workoutName.asStateFlow()

    private val _exercises = MutableStateFlow<List<ActiveExerciseState>>(emptyList())
    val exercises: StateFlow<List<ActiveExerciseState>> = _exercises.asStateFlow()

    private val _startTime = MutableStateFlow(System.currentTimeMillis())

    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _restTimerRunning = MutableStateFlow(false)
    val restTimerRunning: StateFlow<Boolean> = _restTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    var onRestTimerFinished: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            if (existingSessionId > 0) {
                resumeSession(existingSessionId)
            } else {
                createNewSession()
            }
        }
    }

    private suspend fun createNewSession() {
        if (routineId > 0) {
            val routine = repository.getRoutineById(routineId)
            _workoutName.value = routine?.name ?: "Tréning"
            val routineExercises = repository.getExercisesForRoutine(routineId).firstOrNull() ?: emptyList()
            _exercises.value = routineExercises.map { ActiveExerciseState(it) }
        }
        val id = repository.insertSession(
            WorkoutSession(
                name = _workoutName.value,
                routineId = if (routineId > 0) routineId else null,
                startTime = _startTime.value
            )
        )
        _sessionId.value = id
        prefs.save(id, _startTime.value)
        loadPreviousSessionData()
    }


    private suspend fun resumeSession(sessionId: Long) {
        val session = repository.getSessionById(sessionId) ?: return
        _workoutName.value = session.name
        _startTime.value = session.startTime
        _sessionId.value = sessionId

        val savedSets = repository.getSetsForSessionOnce(sessionId)
        val setsByExercise = linkedMapOf<Long, MutableList<ExerciseSet>>()
        savedSets.forEach { set ->
            setsByExercise.getOrPut(set.exerciseId) { mutableListOf() }.add(set)
        }
        val states = setsByExercise.map { (exId, sets) ->
            val exercise = repository.getExerciseById(exId)
                ?: Exercise(id = exId, name = sets.first().exerciseName)
            ActiveExerciseState(exercise = exercise, sets = sets)
        }
        _exercises.value = states
        loadPreviousSessionData()
    }


    private suspend fun loadPreviousSessionData() {
        val sid = _sessionId.value ?: return
        _exercises.value = coroutineScope {
            _exercises.value.map { state ->
                async { state.copy(lastSessionSets = repository.getLastSessionSets(state.exercise.id, sid)) }
            }.awaitAll()
        }
    }

    fun addSet(exerciseIndex: Int, reps: Int, weightKg: Float) {
        val sid = _sessionId.value ?: return
        val list = _exercises.value.toMutableList()
        val state = list[exerciseIndex]
        val setNumber = state.sets.size + 1
        val newSet = ExerciseSet(
            sessionId = sid,
            exerciseId = state.exercise.id,
            exerciseName = state.exercise.name,
            setNumber = setNumber,
            reps = reps,
            weightKg = weightKg
        )
        viewModelScope.launch {
            val setId = repository.insertSet(newSet)
            startRestTimer(90)

            val prevBestWeight = state.lastSessionSets.maxOfOrNull { it.weightKg } ?: 0f
            val prevBestReps = state.lastSessionSets.maxOfOrNull { it.reps } ?: 0
            val progressMsg: String? = when {
                prevBestWeight > 0f && weightKg > prevBestWeight ->
                    "Skvelá práca! Nový rekord: ${weightKg}kg (bolo ${prevBestWeight}kg)"
                prevBestReps > 0 && reps > prevBestReps ->
                    "Zlepšil si sa! ${reps}x opakovaní (bolo ${prevBestReps}x)"
                else -> null
            }

            list[exerciseIndex] = state.copy(
                sets = state.sets + newSet.copy(id = setId),
                progressMessage = progressMsg
            )
            _exercises.value = list.toList()
        }
    }

    fun clearProgressMessage(exerciseIndex: Int) {
        val list = _exercises.value.toMutableList()
        if (exerciseIndex in list.indices) {
            list[exerciseIndex] = list[exerciseIndex].copy(progressMessage = null)
            _exercises.value = list.toList()
        }
    }

    fun removeSet(exerciseIndex: Int, set: ExerciseSet) {
        viewModelScope.launch {
            repository.deleteSet(set)
            val list = _exercises.value.toMutableList()
            if (exerciseIndex in list.indices) {
                list[exerciseIndex] = list[exerciseIndex].copy(
                    sets = list[exerciseIndex].sets.filter { it.id != set.id }
                )
                _exercises.value = list.toList()
            }
        }
    }

    fun moveExerciseUp(index: Int) {
        if (index <= 0) return
        swapExercises(index - 1, index)
    }

    fun moveExerciseDown(index: Int) {
        if (index >= _exercises.value.lastIndex) return
        swapExercises(index, index + 1)
    }

    private fun swapExercises(indexA: Int, indexB: Int) {
        val items = _exercises.value.toMutableList()
        val tmp = items[indexA]
        items[indexA] = items[indexB]
        items[indexB] = tmp
        _exercises.value = items
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            val sid = _sessionId.value ?: return@launch
            val lastSets = repository.getLastSessionSets(exercise.id, sid)
            _exercises.value = _exercises.value + ActiveExerciseState(
                exercise = exercise,
                lastSessionSets = lastSets
            )
        }
    }

    fun addNewExercise(name: String, category: String) {
        viewModelScope.launch {
            val sid = _sessionId.value ?: return@launch
            val id = repository.insertExercise(Exercise(name = name, category = category))
            val exercise = Exercise(id = id, name = name, category = category)
            val lastSets = repository.getLastSessionSets(id, sid)
            _exercises.value = _exercises.value + ActiveExerciseState(
                exercise = exercise,
                lastSessionSets = lastSets
            )
        }
    }

    private fun calculateTotalVolume(): Float =
        _exercises.value.sumOf { state ->
            state.sets.sumOf { set -> (set.reps * set.weightKg).toDouble() }
        }.toFloat()

    fun finishWorkout(onFinished: (sessionId: Long, totalVolumeKg: Float) -> Unit) {
        val sid = _sessionId.value ?: return
        viewModelScope.launch {
            val endTime = System.currentTimeMillis()
            val duration = (endTime - _startTime.value) / 1000
            val totalVolume = calculateTotalVolume()
            val session = repository.getSessionById(sid) ?: return@launch
            repository.updateSession(
                session.copy(endTime = endTime, durationSeconds = duration)
            )
            stopRestTimer()
            prefs.clear()
            onFinished(sid, totalVolume)
        }
    }


    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _restTimerSeconds.value = seconds
        _restTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_restTimerSeconds.value > 0) {
                delay(1000)
                _restTimerSeconds.value -= 1
            }
            _restTimerRunning.value = false
            onRestTimerFinished?.invoke()
        }
    }

    fun stopRestTimer() {
        timerJob?.cancel()
        _restTimerRunning.value = false
        _restTimerSeconds.value = 0
    }


    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val repository: WorkoutRepository,
        private val prefs: ActiveSessionPrefs,
        private val routineId: Long,
        private val existingSessionId: Long = -1L
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ActiveWorkoutViewModel(repository, prefs, routineId, existingSessionId) as T
        }
    }
}
