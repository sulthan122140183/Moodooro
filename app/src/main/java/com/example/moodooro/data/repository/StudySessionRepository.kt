package com.example.moodooro.data.repository

import com.example.moodooro.data.local.StudySessionDao
import com.example.moodooro.data.local.StudySessionEntity
import kotlinx.coroutines.flow.Flow

class StudySessionRepository(private val studySessionDao: StudySessionDao) {

    val allStudySessions: Flow<List<StudySessionEntity>> = studySessionDao.getAllSessions()

    fun getRecentSessions(limit: Int): Flow<List<StudySessionEntity>> {
        return studySessionDao.getRecentSessions(limit)
    }

    suspend fun insertSession(session: StudySessionEntity) {
        studySessionDao.insertSession(session)
    }

    // Anda bisa menambahkan fungsi lain di sini jika diperlukan,
    // misalnya untuk menghapus atau memperbarui sesi.
}