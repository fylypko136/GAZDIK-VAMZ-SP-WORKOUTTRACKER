package com.example.gazdik_vamz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gazdik_vamz.data.local.entity.ExerciseSet
import com.example.gazdik_vamz.data.local.entity.WorkoutSession
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** ViewModel pre obrazovku detailu dokončeného tréningu.*/
// https://www.youtube.com/watch?v=9eIhMFTs1Q8 - Simple MVVM App 🔥 | Android | Jetpack Compose - Easy Tuto
class WorkoutDetailViewModel(
    private val repository: WorkoutRepository,
    private val sessionId: Long
) : ViewModel() {

    private val _session = MutableStateFlow<WorkoutSession?>(null)
    val session: StateFlow<WorkoutSession?> = _session.asStateFlow()


    val sets: StateFlow<List<ExerciseSet>> = repository.getSetsForSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _session.value = repository.getSessionById(sessionId)
        }
    }

    class Factory(
        private val repository: WorkoutRepository,
        private val sessionId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return WorkoutDetailViewModel(repository, sessionId) as T
        }
    }
}
