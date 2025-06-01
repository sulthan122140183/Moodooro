package com.example.moodooro.viewModels

import androidx.lifecycle.*
import com.example.moodooro.data.local.entity.StudySessionEntity
import com.example.moodooro.data.repository.StudySessionRepository
import kotlinx.coroutines.launch

class StudySessionViewModel(private val repository: StudySessionRepository) : ViewModel() {

    private val _insertedSessionId = MutableLiveData<Long?>()
    val insertedSessionId: LiveData<Long?> = _insertedSessionId

    /**
     * Inserts a study session into the database.
     * The ID of the inserted session will be posted to insertedSessionId LiveData.
     */
    fun insertStudySession(session: StudySessionEntity) {
        viewModelScope.launch {
            val id = repository.insertStudySession(session)
            _insertedSessionId.postValue(id)
        }
    }

    /**
     * Updates the mood for a specific session.
     * IMPORTANT: This assumes your StudySessionEntity has a 'mood' field
     * and your StudySessionRepository and StudySessionDao can update it.
     */
    fun updateMoodForSession(sessionId: Long, mood: String) {
        viewModelScope.launch {
            repository.updateMoodForSession(sessionId, mood)
            // You might want to add LiveData to observe success or failure if needed
        }
    }

    // Call this to clear the last inserted ID if you navigate away and back
    // and don't want the old ID to trigger observers again.
    fun clearLastInsertedSessionId() {
        _insertedSessionId.value = null
    }
}
