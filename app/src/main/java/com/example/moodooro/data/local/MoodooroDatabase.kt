package com.example.moodooro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [StudySessionEntity::class, MoodEntryEntity::class], version = 2, exportSchema = false)
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

        fun getDatabase(context: Context): MoodooroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodooroDatabase::class.java,
                    "moodooro_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}