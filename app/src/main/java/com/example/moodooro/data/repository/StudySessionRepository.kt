package com.example.moodooro.data.repository

import androidx.lifecycle.LiveData
import com.example.moodooro.data.local.dao.StudySessionDao
import com.example.moodooro.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

class StudySessionRepository(private val studySessionDao: StudySessionDao) {

    suspend fun insertStudySession(session: StudySessionEntity): Long {
        return studySessionDao.insertStudySession(session)
    }

    suspend fun updateMoodForSession(sessionId: Long, mood: String) {
        studySessionDao.updateMoodForSession(sessionId, mood)
    }

    // Example: Expose LiveData from DAO
    fun getAllStudySessions(): LiveData<List<StudySessionEntity>> {
        return studySessionDao.getAllStudySessions()
    }

    fun getStudySessionById(sessionId: Long): LiveData<StudySessionEntity?> {
        return studySessionDao.getStudySessionById(sessionId)
    }

    fun getRecentSessions(limit: Int): Flow<List<StudySessionEntity>> {
        return studySessionDao.getRecentSessions(limit)
    }

    fun getSessionsAfter(timestamp: Long): Flow<List<StudySessionEntity>> {
        return studySessionDao.getSessionsAfter(timestamp)
    }

    // Add other repository methods to interact with the DAO
}
