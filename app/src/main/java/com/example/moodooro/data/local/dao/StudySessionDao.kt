package com.example.moodooro.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodooro.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long // Returns the new rowId

    // IMPORTANT: Ensure your StudySessionEntity has a 'mood' field for this to work.
    // Example: @ColumnInfo(name = "mood") var mood: String? = null
    @Query("UPDATE study_sessions SET mood = :mood WHERE id = :sessionId")
    suspend fun updateMoodForSession(sessionId: Long, mood: String)

    // Example query: Get all sessions (you might want more specific queries)
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllStudySessions(): LiveData<List<StudySessionEntity>>

    // Example query: Get a session by ID (if needed)
    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    fun getStudySessionById(sessionId: Long): LiveData<StudySessionEntity?> // Nullable if not found

    @Query("SELECT * FROM study_sessions WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    fun getSessionsAfter(timestamp: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<StudySessionEntity>>

    // Add other queries as needed, for example, to get sessions for a specific date,
    // total study time, average mood, etc.
}
