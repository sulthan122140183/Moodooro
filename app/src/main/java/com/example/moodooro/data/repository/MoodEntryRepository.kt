package com.example.moodooro.data.repository

import androidx.lifecycle.LiveData
import com.example.moodooro.data.local.MoodEntryDao // Path diperbarui
import com.example.moodooro.data.local.MoodEntryEntity // Path diperbarui
import kotlinx.coroutines.flow.Flow

/**
 * Repository untuk mengelola data MoodEntryEntity.
 * Kelas ini mengabstraksi akses ke sumber data (dalam hal ini, MoodEntryDao).
 *
 * @property moodEntryDao Instance dari DAO untuk catatan mood.
 */
class MoodEntryRepository(private val moodEntryDao: MoodEntryDao) {

    /**
     * Mengambil semua catatan mood sebagai Flow.
     * @return Flow yang berisi List dari MoodEntryEntity.
     */
    val allMoodEntries: Flow<List<MoodEntryEntity>> = moodEntryDao.getAllMoodEntries()

    /**
     * Menyisipkan satu catatan mood ke database.
     * @param moodEntry Entitas catatan mood yang akan disisipkan.
     * @return Long ID dari catatan mood yang baru disisipkan.
     */
    suspend fun insert(moodEntry: MoodEntryEntity): Long {
        return moodEntryDao.insertMoodEntry(moodEntry)
    }

    /**
     * Memperbarui catatan mood yang sudah ada.
     * @param moodEntry Entitas catatan mood yang akan diperbarui.
     */
    suspend fun update(moodEntry: MoodEntryEntity) {
        moodEntryDao.updateMoodEntry(moodEntry)
    }

    /**
     * Menghapus catatan mood.
     * @param moodEntry Entitas catatan mood yang akan dihapus.
     */
    suspend fun delete(moodEntry: MoodEntryEntity) {
        moodEntryDao.deleteMoodEntry(moodEntry)
    }

    /**
     * Menghapus semua catatan mood dari database.
     */
    suspend fun deleteAllMoodEntries() {
        moodEntryDao.deleteAllMoodEntries()
    }

    /**
     * Mengambil catatan mood berdasarkan ID-nya sebagai Flow.
     * @param moodId ID dari catatan mood yang dicari.
     * @return Flow yang berisi MoodEntryEntity atau null.
     */
    fun getMoodEntryById(moodId: Long): Flow<MoodEntryEntity?> {
        return moodEntryDao.getMoodEntryById(moodId)
    }

    /**
     * Mengambil semua catatan mood yang terkait dengan ID sesi belajar tertentu sebagai Flow.
     * @param sessionId ID dari sesi belajar.
     * @return Flow yang berisi List dari MoodEntryEntity.
     */
    fun getMoodEntriesBySessionId(sessionId: Long): Flow<List<MoodEntryEntity>> {
        return moodEntryDao.getMoodEntriesBySessionId(sessionId)
    }

    /**
     * Mengambil catatan mood dalam rentang tanggal tertentu sebagai LiveData.
     * @param startDateMillis Waktu mulai rentang.
     * @param endDateMillis Waktu akhir rentang.
     * @return LiveData yang berisi List dari MoodEntryEntity.
     */
    fun getMoodEntriesByDateRange(startDateMillis: Long, endDateMillis: Long): LiveData<List<MoodEntryEntity>> {
        return moodEntryDao.getMoodEntriesByDateRange(startDateMillis, endDateMillis)
    }
}