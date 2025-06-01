package com.example.moodooro.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.moodooro.data.model.StudySession // Removed
// import com.example.moodooro.data.model.MoodEntry // Removed
import com.example.moodooro.data.repository.MoodEntryRepository
import com.example.moodooro.data.repository.StudySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Placeholder data class if you don't have specific UI models yet
// You might want to map your Entities to simpler data classes for the UI
// data class StudySessionUI(val id: Long, val title: String, val duration: String, val status: String, val iconName: String)
// data class MoodEntryUI(val id: Long, val mood: String, val notes: String?)

class MainViewModel(
    private val studySessionRepository: StudySessionRepository,
    private val moodEntryRepository: MoodEntryRepository
) : ViewModel() {

    // Example: StateFlow for recent study sessions
    private val _recentStudySessions = MutableStateFlow<List<com.example.moodooro.data.local.entity.StudySessionEntity>>(emptyList())
    val recentStudySessions: StateFlow<List<com.example.moodooro.data.local.entity.StudySessionEntity>> = _recentStudySessions.asStateFlow()

    // Example: StateFlow for mood entries (e.g., last 7 days)
    // private val _moodHistory = MutableStateFlow<List<com.example.moodooro.data.local.entity.MoodEntryEntity>>(emptyList())
    // val moodHistory: StateFlow<List<com.example.moodooro.data.local.entity.MoodEntryEntity>> = _moodHistory.asStateFlow()

    // Example: Combined state for a dashboard or overview
    // data class DashboardData(
    // val recentSessions: List<com.example.moodooro.data.local.entity.StudySessionEntity>,
    // val moodInsights: List<com.example.moodooro.data.local.entity.MoodEntryEntity> // Or some processed insight
    // )
    // private val _dashboardData = MutableStateFlow<DashboardData?>(null)
    // val dashboardData: StateFlow<DashboardData?> = _dashboardData.asStateFlow()

    init {
        loadRecentStudySessions()
        // loadMoodHistory()
        // loadDashboardData()
    }

    private fun loadRecentStudySessions() {
        viewModelScope.launch {
            studySessionRepository.getRecentSessions(limit = 5).collect { sessions ->
                _recentStudySessions.value = sessions
            }
        }
    }

    // private fun loadMoodHistory() {
    //     viewModelScope.launch {
    //         // Example: Fetch moods from the last 7 days
    //         val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
    //         moodEntryRepository.getMoodsAfter(oneWeekAgo).collect { moods ->
    //             _moodHistory.value = moods
    //         }
    //     }
    // }

    // private fun loadDashboardData() {
    //     viewModelScope.launch {
    //         combine(
    //             studySessionRepository.getRecentSessions(limit = 3),
    //             moodEntryRepository.getRecentMoods(limit = 3) // Assuming a similar function in MoodEntryRepository
    //         ) { sessions, moods ->
    //             DashboardData(recentSessions = sessions, moodInsights = moods)
    //         }.collect { combinedData ->
    //             _dashboardData.value = combinedData
    //         }
    //     }
    // }

    // TODO: Add functions to insert/update study sessions and mood entries
    // For example:
    // fun addStudySession(session: com.example.moodooro.data.local.entity.StudySessionEntity) {
    //     viewModelScope.launch {
    //         studySessionRepository.insertSession(session)
    //     }
    // }

    // fun addMoodEntry(mood: com.example.moodooro.data.local.entity.MoodEntryEntity) {
    //     viewModelScope.launch {
    //         moodEntryRepository.insertMood(mood)
    //     }
    // }
}
