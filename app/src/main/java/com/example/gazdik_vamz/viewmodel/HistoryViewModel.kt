package com.example.gazdik_vamz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.gazdik_vamz.data.local.entity.WorkoutSession
import com.example.gazdik_vamz.data.repository.WorkoutRepository
import kotlinx.coroutines.launch

/** ViewModel pre obrazovku histórie tréningov. */
// https://www.youtube.com/watch?v=NlzVx8q1YVg - The Ultimate Guide to Android Pagination with Paging 3 - CodeWithSaid

class HistoryViewModel(private val repository: WorkoutRepository) : ViewModel() {


    val sessions = repository.getPagedSessionSummaries().cachedIn(viewModelScope)


    fun deleteSession(session: WorkoutSession) {
        viewModelScope.launch { repository.deleteSession(session) }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
    }
}
