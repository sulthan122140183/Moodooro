package com.example.moodooro.data.local.entity // Pastikan package name ini sesuai dengan lokasi file Anda

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entitas yang merepresentasikan tabel 'mood_entries' di database.
 * Entitas ini akan menyimpan catatan kondisi emosional (mood) pengguna.
 * Setiap catatan mood dapat dikaitkan dengan sebuah sesi belajar tertentu atau berdiri sendiri.
 */
@Entity(
    tableName = "mood_entries", // Nama tabel di database
    foreignKeys = [
        // Mendefinisikan foreign key ke tabel 'study_sessions'.
        // Ini berarti setiap catatan mood bisa (tapi tidak harus) terkait dengan satu sesi belajar.
        ForeignKey(
            entity = StudySessionEntity::class,      // Entitas induk yang direferensikan
            parentColumns = ["id"],                // Kolom primary key di entitas induk (StudySessionEntity)
            childColumns = ["associated_session_id"],// Kolom foreign key di entitas ini (MoodEntryEntity)
            onDelete = ForeignKey.SET_NULL      // Jika StudySessionEntity yang terkait dihapus,
            // nilai associated_session_id di MoodEntryEntity akan diatur menjadi NULL.
            // Ini mencegah error integritas data dan memungkinkan mood entry tetap ada
            // meskipun sesi belajarnya sudah tidak ada.
        )
    ]
)
data class MoodEntryEntity(
    // Primary Key untuk tabel ini, akan digenerate secara otomatis oleh Room.
    // Setiap catatan mood akan memiliki ID yang unik.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Menyimpan waktu ketika mood ini dicatat oleh pengguna, dalam format milidetik (epoch time).
    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long,

    // Menyimpan nilai atau deskripsi dari mood yang dicatat.
    // Contoh: "Senang", "Biasa", "Tidak Fokus", "Stres", "Produktif", dll.
    // Tipe data String memberikan fleksibilitas.
    @ColumnInfo(name = "mood_value")
    val moodValue: String,

    // Foreign key yang menghubungkan ke ID dari StudySessionEntity.
    // Dibuat nullable (Long?) karena mood bisa dicatat kapan saja, tidak harus selalu setelah sesi belajar.
    // 'index = true' dapat membantu meningkatkan performa query yang menggunakan kolom ini sebagai kriteria filter.
    @ColumnInfo(name = "associated_session_id", index = true)
    val associatedSessionId: Long? = null,

    // Catatan tambahan atau detail yang mungkin ingin ditulis pengguna terkait mood mereka.
    // Dibuat nullable (String?) karena ini adalah input opsional.
    @ColumnInfo(name = "notes")
    val notes: String? = null
)