package com.example.gazdik_vamz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gazdik_vamz.data.local.entity.Exercise
import com.example.gazdik_vamz.data.local.entity.WorkoutRoutine
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** ViewModel pre obrazovku vytvorenia/úpravy rutiny.*/
// https://www.youtube.com/watch?v=9eIhMFTs1Q8 - Simple MVVM App | Android | Jetpack Compose - Easy Tuto

class RoutineDetailViewModel(
    private val repository: WorkoutRepository,
    private val routineId: Long
) : ViewModel() {

    private val _routine = MutableStateFlow<WorkoutRoutine?>(null)
    val routine: StateFlow<WorkoutRoutine?> = _routine.asStateFlow()


    val exercises: StateFlow<List<Exercise>> = _routine
        .flatMapLatest { routine ->
            if (routine != null) repository.getExercisesForRoutine(routine.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (routineId > 0) {
            viewModelScope.launch {
                _routine.value = repository.getRoutineById(routineId)
            }
        }
    }

    fun saveRoutine(name: String, description: String, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val existing = _routine.value
            val id = if (existing != null) {
                repository.updateRoutine(existing.copy(name = name, description = description))
                existing.id
            } else {
                repository.insertRoutine(WorkoutRoutine(name = name, description = description))
            }
            _routine.value = repository.getRoutineById(id)
            onSaved(id)
        }
    }

    fun addNewExercise(name: String, category: String) {
        viewModelScope.launch {
            val id = repository.insertExercise(Exercise(name = name, category = category))
            val currentSize = exercises.value.size
            repository.addExerciseToRoutine(routine.value?.id ?: return@launch, id, currentSize)
        }
    }


    fun addExistingExercise(exercise: Exercise) {
        viewModelScope.launch {
            val rid = routine.value?.id ?: return@launch
            val currentSize = exercises.value.size
            repository.addExerciseToRoutine(rid, exercise.id, currentSize)
        }
    }


    fun removeExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.removeExerciseFromRoutine(routine.value?.id ?: return@launch, exercise.id)
        }
    }


    fun moveExerciseUp(exercise: Exercise) {
        val rid = routine.value?.id ?: return
        val list = exercises.value
        val idx = list.indexOfFirst { it.id == exercise.id }
        if (idx <= 0) return // Už je na prvom mieste — nemôže ísť vyššie
        viewModelScope.launch {
            repository.updateExerciseOrder(rid, list[idx].id, idx - 1)
            repository.updateExerciseOrder(rid, list[idx - 1].id, idx)
        }
    }


    fun moveExerciseDown(exercise: Exercise) {
        val rid = routine.value?.id ?: return
        val list = exercises.value
        val idx = list.indexOfFirst { it.id == exercise.id }
        if (idx < 0 || idx >= list.lastIndex) return // Už je na poslednom mieste
        viewModelScope.launch {
            repository.updateExerciseOrder(rid, list[idx].id, idx + 1)
            repository.updateExerciseOrder(rid, list[idx + 1].id, idx)
        }
    }

    class Factory(
        private val repository: WorkoutRepository,
        private val routineId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RoutineDetailViewModel(repository, routineId) as T
        }
    }
}
