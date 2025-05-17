package com.example.moodooro.data.local.dao // Pastikan package name sesuai

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moodooro.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) untuk entitas MoodEntryEntity.
 * Interface ini mendefinisikan semua operasi database yang terkait dengan catatan mood.
 */
@Dao
interface MoodEntryDao {

    /**
     * Menyisipkan satu catatan mood ke dalam database.
     * Jika terjadi konflik, data lama akan diganti.
     * @param moodEntry Entitas catatan mood yang akan disisipkan.
     * @return Long ID dari baris yang baru disisipkan.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(moodEntry: MoodEntryEntity): Long

    /**
     * Memperbarui data catatan mood yang sudah ada di database.
     * @param moodEntry Entitas catatan mood dengan data yang sudah diperbarui.
     */
    @Update
    suspend fun updateMoodEntry(moodEntry: MoodEntryEntity)

    /**
     * Menghapus satu catatan mood dari database.
     * @param moodEntry Entitas catatan mood yang akan dihapus.
     */
    @Delete
    suspend fun deleteMoodEntry(moodEntry: MoodEntryEntity)

    /**
     * Menghapus semua catatan mood dari tabel mood_entries.
     */
    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllMoodEntries()

    /**
     * Mengambil satu catatan mood berdasarkan ID-nya.
     * @param moodId ID dari catatan mood yang dicari.
     * @return Flow yang memancarkan MoodEntryEntity atau null jika tidak ditemukan.
     */
    @Query("SELECT * FROM mood_entries WHERE id = :moodId")
    fun getMoodEntryById(moodId: Long): Flow<MoodEntryEntity?>

    /**
     * Mengambil semua catatan mood dari database, diurutkan berdasarkan waktu (terbaru dulu).
     * @return Flow yang memancarkan List dari MoodEntryEntity.
     */
    @Query("SELECT * FROM mood_entries ORDER BY timestamp_millis DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntryEntity>>

    /**
     * Mengambil semua catatan mood yang terkait dengan ID sesi belajar tertentu.
     * @param sessionId ID dari sesi belajar.
     * @return Flow yang memancarkan List dari MoodEntryEntity.
     */
    @Query("SELECT * FROM mood_entries WHERE associated_session_id = :sessionId ORDER BY timestamp_millis DESC")
    fun getMoodEntriesBySessionId(sessionId: Long): Flow<List<MoodEntryEntity>>

    /**
     * Mengambil semua catatan mood dalam rentang tanggal tertentu.
     * @param startDateMillis Waktu mulai rentang (epoch millis).
     * @param endDateMillis Waktu akhir rentang (epoch millis).
     * @return LiveData yang berisi List dari MoodEntryEntity.
     */
    @Query("SELECT * FROM mood_entries WHERE timestamp_millis BETWEEN :startDateMillis AND :endDateMillis ORDER BY timestamp_millis DESC")
    fun getMoodEntriesByDateRange(startDateMillis: Long, endDateMillis: Long): LiveData<List<MoodEntryEntity>>
}