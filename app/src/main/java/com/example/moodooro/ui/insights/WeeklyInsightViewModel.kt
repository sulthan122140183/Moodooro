package com.example.moodooro.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moodooro.data.local.dao.StudySessionDao
import com.example.moodooro.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.catch
// import kotlinx.coroutines.flow.combine // Not used
import java.util.Calendar // Added import

data class WeeklyStats(
    val totalStudyDurationMinutes: Int = 0,
    val focusedSessionsCount: Int = 0,
    val distractedSessionsCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class WeeklyInsightViewModel(private val studySessionDao: StudySessionDao) : ViewModel() {

    private val _weeklyStats = MutableStateFlow(WeeklyStats())
    val weeklyStats: StateFlow<WeeklyStats> = _weeklyStats.asStateFlow()

    private val _studySessions = MutableStateFlow<List<StudySessionEntity>>(emptyList())
    val studySessions: StateFlow<List<StudySessionEntity>> = _studySessions.asStateFlow()

    init {
        loadWeeklyData()
    }

    fun loadWeeklyData() {
        _weeklyStats.value = WeeklyStats(isLoading = true, error = null)
        _studySessions.value = emptyList() // Clear previous sessions

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // Go back 7 days
        val sevenDaysAgoTimestamp = calendar.timeInMillis

        studySessionDao.getSessionsAfter(sevenDaysAgoTimestamp) // Use existing DAO method
            .onEach { sessions: List<StudySessionEntity> -> // Explicitly typed sessions
                _studySessions.value = sessions // Update study sessions

                if (sessions.isEmpty()) {
                    _weeklyStats.value = WeeklyStats(isLoading = false)
                } else {
                    val totalDuration = sessions.sumOf { session: StudySessionEntity -> session.focusDurationMinutes }
                    val focusedCount = sessions.count { session: StudySessionEntity -> session.sessionOutcome == "Focused" }
                    val distractedCount = sessions.count { session: StudySessionEntity -> session.sessionOutcome == "Distracted" }
                    _weeklyStats.value = WeeklyStats(
                        totalStudyDurationMinutes = totalDuration,
                        focusedSessionsCount = focusedCount,
                        distractedSessionsCount = distractedCount,
                        isLoading = false
                    )
                }
            }
            .catch { e: Throwable -> // Explicitly typed exception
                _weeklyStats.value = WeeklyStats(isLoading = false, error = "Gagal memuat data statistik: ${e.localizedMessage}")
                // Anda mungkin juga ingin menandai error pada _studySessions jika perlu
                 _studySessions.value = emptyList() // Kosongkan sesi jika ada error
            }
            .launchIn(viewModelScope)
    }
}

class WeeklyInsightViewModelFactory(private val studySessionDao: StudySessionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeeklyInsightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeeklyInsightViewModel(studySessionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
