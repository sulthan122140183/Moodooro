package com.example.moodooro.ui.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodooro.data.local.MoodooroDatabase
import com.example.moodooro.data.local.entity.MoodEntryEntity
import com.example.moodooro.data.local.entity.StudySessionEntity
import com.example.moodooro.data.repository.MoodEntryRepository
import com.example.moodooro.data.repository.StudySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DailyMoodData(
    val date: Long,
    val averageMoodScore: Float,
    val moodLabel: String
)

class MainViewModel(
    application: Application // Accept Application context
) : ViewModel() {

    // Repositories will be initialized after the database is ready
    private var studySessionRepository: StudySessionRepository? = null
    private var moodEntryRepository: MoodEntryRepository? = null

    private val _recentStudySessions = MutableStateFlow<List<StudySessionEntity>>(emptyList())
    val recentStudySessions: StateFlow<List<StudySessionEntity>> = _recentStudySessions.asStateFlow()

    private val _moodInsights = MutableStateFlow<List<DailyMoodData>>(emptyList())
    val moodInsights: StateFlow<List<DailyMoodData>> = _moodInsights.asStateFlow()

    private val _averageDailyStudyTimeMinutes = MutableStateFlow<Int>(0)
    val averageDailyStudyTimeMinutes: StateFlow<Int> = _averageDailyStudyTimeMinutes.asStateFlow()

    // State to indicate if data is ready/loading, can be more granular if needed
    private val _isDataLoading = MutableStateFlow(true)
    val isDataLoading: StateFlow<Boolean> = _isDataLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _isDataLoading.value = true
            // Get database instance asynchronously
            val database = MoodooroDatabase.getSuspendingDatabase(application.applicationContext)
            // Initialize repositories
            studySessionRepository = StudySessionRepository(database.studySessionDao())
            moodEntryRepository = MoodEntryRepository(database.moodEntryDao())

            // Now that repositories are initialized, load data
            loadRecentStudySessions()
            loadMoodInsights()
            loadWeeklyStudyInsights()
            _isDataLoading.value = false
        }
    }

    private fun loadRecentStudySessions() {
        viewModelScope.launch {
            studySessionRepository?.getRecentSessions(limit = 5)?.collect { sessions: List<StudySessionEntity> ->
                _recentStudySessions.value = sessions
            }
        }
    }

    private fun loadWeeklyStudyInsights() {
        viewModelScope.launch {
            val sevenDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
            }.timeInMillis

            studySessionRepository?.getSessionsAfter(sevenDaysAgo)?.collect { sessions: List<StudySessionEntity> ->
                val totalMinutes = sessions.sumOf { session: StudySessionEntity -> session.focusDurationMinutes }
                _averageDailyStudyTimeMinutes.value = if (sessions.isNotEmpty()) {
                    totalMinutes / 7 // Calculate average over 7 days
                } else {
                    0
                }
            }
        }
    }

    private fun loadMoodInsights() {
        viewModelScope.launch {
            moodEntryRepository?.allMoodEntries?.collect { moodEntries: List<MoodEntryEntity> ->
                if (moodEntries.isEmpty()) {
                    _moodInsights.value = emptyList()
                    return@collect
                }

                val groupedByDay = moodEntries.groupBy {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = it.timestampMillis
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }

                val dailyMoodDataList = groupedByDay.map { (dayTimestamp, entriesOnDay) ->
                    if (entriesOnDay.isNotEmpty()) {
                        val averageScore = entriesOnDay.map { entry: MoodEntryEntity ->
                            when (entry.moodValue.lowercase()) {
                                "bagus" -> 1.0f
                                "baik" -> 1.0f
                                "biasa" -> 0.5f
                                "tidak baik" -> 0.25f
                                "buruk" -> 0.1f
                                else -> 0.0f
                            }
                        }.average().toFloat()

                        val moodLabel = when {
                            averageScore > 0.7f -> "Bagus"
                            averageScore > 0.5f -> "Baik"
                            averageScore >= 0.35f -> "Biasa"
                            averageScore >= 0.15f -> "Tidak Baik"
                            else -> "Buruk"
                        }
                        DailyMoodData(date = dayTimestamp, averageMoodScore = averageScore, moodLabel = moodLabel)
                    } else {
                        DailyMoodData(date = dayTimestamp, averageMoodScore = 0f, moodLabel = "N/A")
                    }
                }.sortedByDescending { moodData: DailyMoodData -> moodData.date }
                
                _moodInsights.value = dailyMoodDataList.take(7)
            }
        }
    }
}
