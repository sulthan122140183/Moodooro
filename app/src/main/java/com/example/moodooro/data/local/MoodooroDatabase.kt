package com.example.moodooro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moodooro.data.local.dao.MoodEntryDao
import com.example.moodooro.data.local.dao.StudySessionDao
import com.example.moodooro.data.local.entity.MoodEntryEntity
import com.example.moodooro.data.local.entity.StudySessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 1. NAIKKAN VERSI DATABASE MENJADI 3
@Database(entities = [StudySessionEntity::class, MoodEntryEntity::class], version = 3, exportSchema = false)
abstract class MoodooroDatabase : RoomDatabase() {

    abstract fun studySessionDao(): StudySessionDao
    abstract fun moodEntryDao(): MoodEntryDao

    companion object {
        @Volatile
        private var INSTANCE: MoodooroDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_sessions ADD COLUMN session_outcome TEXT NOT NULL DEFAULT '''PENDING'''")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_sessions ADD COLUMN mood TEXT")
            }
        }

        suspend fun getSuspendingDatabase(context: Context): MoodooroDatabase {
            // First check (no lock)
            INSTANCE?.let { return it }

            // If INSTANCE is null, build the database
            // The buildDatabase function is already suspendable and uses Dispatchers.IO
            val instance = buildDatabase(context)

            // Now synchronize only the assignment to INSTANCE
            return synchronized(this) {
                INSTANCE?.let { return@synchronized it } // Double-check
                INSTANCE = instance
                instance
            }
        }

        // Separate build function to be called from a coroutine
        private suspend fun buildDatabase(context: Context): MoodooroDatabase {
            return withContext(Dispatchers.IO) { // Ensure build happens off main thread
                Room.databaseBuilder(
                    context.applicationContext,
                    MoodooroDatabase::class.java,
                    "moodooro_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
            }
        }
    }
}