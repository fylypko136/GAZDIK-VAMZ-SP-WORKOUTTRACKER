package com.example.gazdik_vamz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gazdik_vamz.data.local.entity.Exercise
import com.example.gazdik_vamz.data.local.entity.SessionSetCount
import com.example.gazdik_vamz.data.local.entity.WeightProgressEntry
import com.example.gazdik_vamz.data.local.entity.WorkoutSession
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


enum class ChartType {
    DURATION,
    SETS
}


class StatsViewModel(private val repository: WorkoutRepository) : ViewModel() {

    val totalSessions = repository.getTotalSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sessionsThisWeek: StateFlow<List<WorkoutSession>> = repository
        .getSessionsSince(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionsThisMonth: StateFlow<List<WorkoutSession>> = repository
        .getSessionsSince(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionSetCounts: StateFlow<List<SessionSetCount>> = repository.getSessionSetCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<Exercise>> = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _chartType = MutableStateFlow(ChartType.DURATION)
    val chartType: StateFlow<ChartType> = _chartType.asStateFlow()


    fun toggleChartType() {
        _chartType.value = if (_chartType.value == ChartType.DURATION) ChartType.SETS else ChartType.DURATION
    }


    private val _selectedExercise = MutableStateFlow<Exercise?>(null)
    val selectedExercise: StateFlow<Exercise?> = _selectedExercise.asStateFlow()

    private val _weightProgress = MutableStateFlow<List<WeightProgressEntry>>(emptyList())
    val weightProgress: StateFlow<List<WeightProgressEntry>> = _weightProgress.asStateFlow()


    fun selectExercise(exercise: Exercise?) {
        _selectedExercise.value = exercise
        viewModelScope.launch {
            _weightProgress.value = if (exercise != null)
                repository.getWeightProgressionForExercise(exercise.id)
            else emptyList()
        }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(repository) as T
        }
    }
}
