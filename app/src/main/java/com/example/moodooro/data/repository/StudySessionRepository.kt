package com.example.moodooro.data.repository // Pastikan package name sesuai

import androidx.lifecycle.LiveData
import com.example.moodooro.data.local.dao.StudySessionDao
import com.example.moodooro.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository untuk mengelola data StudySessionEntity.
 * Kelas ini mengabstraksi akses ke sumber data (dalam hal ini, StudySessionDao).
 * Semua operasi data terkait sesi belajar akan melalui repository ini.
 *
 * @property studySessionDao Instance dari DAO untuk sesi belajar.
 */
class StudySessionRepository(private val studySessionDao: StudySessionDao) {

    /**
     * Mengambil semua sesi belajar sebagai Flow.
     * Flow akan secara otomatis memancarkan data baru jika ada perubahan di database.
     * @return Flow yang berisi List dari StudySessionEntity.
     */
    val allStudySessions: Flow<List<StudySessionEntity>> = studySessionDao.getAllSessions()

    /**
     * Mengambil sesi belajar terbaru sebagai Flow.
     * @return Flow yang berisi StudySessionEntity terbaru atau null.
     */
    val latestStudySession: Flow<StudySessionEntity?> = studySessionDao.getLatestSession()

    /**
     * Menyisipkan satu sesi belajar ke database.
     * Operasi ini dijalankan dalam coroutine (ditandai dengan suspend).
     * @param session Entitas sesi belajar yang akan disisipkan.
     * @return Long ID dari sesi yang baru disisipkan.
     */
    suspend fun insert(session: StudySessionEntity): Long {
        return studySessionDao.insertSession(session)
    }

    /**
     * Memperbarui sesi belajar yang sudah ada.
     * @param session Entitas sesi belajar yang akan diperbarui.
     */
    suspend fun update(session: StudySessionEntity) {
        studySessionDao.updateSession(session)
    }

    /**
     * Menghapus sesi belajar.
     * @param session Entitas sesi belajar yang akan dihapus.
     */
    suspend fun delete(session: StudySessionEntity) {
        studySessionDao.deleteSession(session)
    }

    /**
     * Menghapus semua sesi belajar dari database.
     */
    suspend fun deleteAllSessions() {
        studySessionDao.deleteAllSessions()
    }

    /**
     * Mengambil sesi belajar berdasarkan ID-nya sebagai Flow.
     * @param sessionId ID dari sesi yang dicari.
     * @return Flow yang berisi StudySessionEntity atau null.
     */
    fun getSessionById(sessionId: Long): Flow<StudySessionEntity?> {
        return studySessionDao.getSessionById(sessionId)
    }

    /**
     * Mengambil sesi belajar dalam rentang tanggal tertentu sebagai LiveData.
     * @param startDateMillis Waktu mulai rentang.
     * @param endDateMillis Waktu akhir rentang.
     * @return LiveData yang berisi List dari StudySessionEntity.
     */
    fun getSessionsByDateRange(startDateMillis: Long, endDateMillis: Long): LiveData<List<StudySessionEntity>> {
        return studySessionDao.getSessionsByDateRange(startDateMillis, endDateMillis)
    }

    /**
     * Mengambil beberapa sesi belajar terbaru, diurutkan berdasarkan waktu mulai (terbaru dulu), dengan jumlah terbatas.
     * @param limit Jumlah maksimal sesi yang akan diambil.
     * @return Flow yang memancarkan List dari StudySessionEntity.
     */
    fun getRecentSessions(limit: Int): Flow<List<StudySessionEntity>> {
        return studySessionDao.getRecentSessions(limit)
    }
}