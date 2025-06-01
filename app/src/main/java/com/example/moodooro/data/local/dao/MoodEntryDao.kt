package com.example.moodooro.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.moodooro.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(moodEntry: MoodEntryEntity): Long

    @Update
    suspend fun updateMoodEntry(moodEntry: MoodEntryEntity)

    @Delete
    suspend fun deleteMoodEntry(moodEntry: MoodEntryEntity)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllMoodEntries()

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    fun getMoodEntryById(id: Long): Flow<MoodEntryEntity?>

    @Query("SELECT * FROM mood_entries") // Bagian ORDER BY telah dihapus
    fun getAllMoodEntries(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE associated_session_id = :sessionId ORDER BY timestamp_millis DESC")
    fun getMoodEntriesBySessionId(sessionId: Long): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE timestamp_millis >= :startDateMillis AND timestamp_millis <= :endDateMillis ORDER BY timestamp_millis DESC")
    fun getMoodEntriesByDateRange(startDateMillis: Long, endDateMillis: Long): LiveData<List<MoodEntryEntity>>
    
    // Example of a query that might be useful for analysis:
    // Get mood entries associated with sessions that have a specific outcome
    @Query("""
        SELECT me.* FROM mood_entries me
        INNER JOIN study_sessions ss ON me.associated_session_id = ss.id
        WHERE ss.session_outcome = :sessionOutcome
        ORDER BY me.timestamp_millis DESC
    """)
    fun getMoodEntriesBySessionOutcome(sessionOutcome: String): Flow<List<MoodEntryEntity>>
}
