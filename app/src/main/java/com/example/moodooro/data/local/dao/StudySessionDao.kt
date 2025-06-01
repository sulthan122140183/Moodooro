package com.example.moodooro.data.local.dao // Pastikan package name sesuai

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moodooro.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) untuk entitas StudySessionEntity.
 * Interface ini mendefinisikan semua operasi database yang terkait dengan sesi belajar.
 */
@Dao
interface StudySessionDao {

    /**
     * Menyisipkan satu sesi belajar ke dalam database.
     * Jika terjadi konflik (misalnya, ID yang sama sudah ada), data lama akan diganti.
     * @param session Entitas sesi belajar yang akan disisipkan.
     * @return Long ID dari baris yang baru disisipkan.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    /**
     * Menyisipkan beberapa sesi belajar ke dalam database.
     * @param sessions List dari entitas sesi belajar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSessions(sessions: List<StudySessionEntity>)

    /**
     * Memperbarui data sesi belajar yang sudah ada di database.
     * @param session Entitas sesi belajar dengan data yang sudah diperbarui.
     */
    @Update
    suspend fun updateSession(session: StudySessionEntity)

    /**
     * Menghapus satu sesi belajar dari database.
     * @param session Entitas sesi belajar yang akan dihapus.
     */
    @Delete
    suspend fun deleteSession(session: StudySessionEntity)

    /**
     * Menghapus semua sesi belajar dari tabel study_sessions.
     * Hati-hati menggunakan fungsi ini karena akan menghapus semua data sesi.
     */
    @Query("DELETE FROM study_sessions")
    suspend fun deleteAllSessions()

    /**
     * Mengambil satu sesi belajar berdasarkan ID-nya.
     * Menggunakan Flow agar UI dapat bereaksi terhadap perubahan data secara otomatis.
     * @param sessionId ID dari sesi belajar yang dicari.
     * @return Flow yang memancarkan StudySessionEntity atau null jika tidak ditemukan.
     */
    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): Flow<StudySessionEntity?>

    /**
     * Mengambil semua sesi belajar dari database, diurutkan berdasarkan waktu mulai (terbaru dulu).
     * Menggunakan Flow agar UI dapat bereaksi terhadap perubahan data (penambahan, penghapusan, pembaruan).
     * @return Flow yang memancarkan List dari StudySessionEntity.
     */
    @Query("SELECT * FROM study_sessions ORDER BY start_time_millis DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    /**
     * Mengambil semua sesi belajar dalam rentang tanggal tertentu.
     * @param startDateMillis Waktu mulai rentang (epoch millis).
     * @param endDateMillis Waktu akhir rentang (epoch millis).
     * @return LiveData yang berisi List dari StudySessionEntity.
     */
    @Query("SELECT * FROM study_sessions WHERE date_millis BETWEEN :startDateMillis AND :endDateMillis ORDER BY start_time_millis DESC")
    fun getSessionsByDateRange(startDateMillis: Long, endDateMillis: Long): LiveData<List<StudySessionEntity>>

    /**
     * Mengambil sesi belajar terbaru.
     * @return Flow yang memancarkan StudySessionEntity terbaru atau null jika tabel kosong.
     */
    @Query("SELECT * FROM study_sessions ORDER BY start_time_millis DESC LIMIT 1")
    fun getLatestSession(): Flow<StudySessionEntity?>

    /**
     * Mengambil beberapa sesi belajar terbaru, diurutkan berdasarkan waktu mulai (terbaru dulu), dengan jumlah terbatas.
     * @param limit Jumlah maksimal sesi yang akan diambil.
     * @return Flow yang memancarkan List dari StudySessionEntity.
     */
    @Query("SELECT * FROM study_sessions ORDER BY start_time_millis DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<StudySessionEntity>>
}