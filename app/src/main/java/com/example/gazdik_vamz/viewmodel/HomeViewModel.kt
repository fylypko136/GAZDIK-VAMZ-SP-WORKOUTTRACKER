package com.example.gazdik_vamz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gazdik_vamz.ActiveSessionPrefs
import com.example.gazdik_vamz.data.local.entity.WorkoutRoutine
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel pre hlavnú obrazovku. */
// https://www.youtube.com/watch?v=9eIhMFTs1Q8 - Simple MVVM App | Android | Jetpack Compose - Easy Tuto

class HomeViewModel(
    private val repository: WorkoutRepository,
    private val prefs: ActiveSessionPrefs
) : ViewModel() {


    val routines = repository.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSessionCount = repository.getTotalSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _activeSessionId = MutableStateFlow<Long?>(null)
    /** ID aktívneho tréningu, alebo null ak žiadny neprebieha. Riadi zobrazenie bannera. */
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    init {
        refreshActiveSession()
    }


    fun refreshActiveSession() {
        _activeSessionId.value = if (prefs.hasValidSession()) prefs.getSessionId() else null
    }


    fun deleteRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch { repository.deleteRoutine(routine) }
    }

    class Factory(
        private val repository: WorkoutRepository,
        private val prefs: ActiveSessionPrefs
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, prefs) as T
        }
    }
}
