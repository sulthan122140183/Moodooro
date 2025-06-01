package com.example.moodooro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long, // Waktu sesi selesai
    val durationMillis: Long, // Durasi sesi dalam milidetik
    val focusStatus: String // "Focused" atau "Distracted"

)