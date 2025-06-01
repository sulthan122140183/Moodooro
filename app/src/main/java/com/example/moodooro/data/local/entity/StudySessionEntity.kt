package com.example.moodooro.data.local.entity // Pastikan package name ini sesuai dengan lokasi file Anda

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas yang merepresentasikan tabel 'study_sessions' di database.
 * Entitas ini akan menyimpan semua informasi terkait setiap sesi belajar Pomodoro
 * yang dilakukan oleh pengguna.
 */
@Entity(tableName = "study_sessions") // Anotasi untuk menandakan kelas ini sebagai tabel database Room
data class StudySessionEntity(
    // Primary Key untuk tabel ini, akan digenerate secara otomatis oleh Room.
    // Setiap sesi belajar akan memiliki ID yang unik.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Waktu sesi dimulai atau dicatat, bisa disesuaikan dengan kebutuhan (misalnya, timestamp selesai)
    val timestamp: Long, // Waktu sesi selesai

    // Durasi sesi dalam milidetik. Sebaiknya ini adalah durasi fokus yang sebenarnya.
    val durationMillis: Long, // Durasi sesi dalam milidetik

    // Status fokus selama sesi, contoh: "Focused", "Distracted"
    val focusStatus: String,

    // Menyimpan waktu mulai sesi belajar dalam format milidetik (epoch time).
    @ColumnInfo(name = "start_time_millis")
    val startTimeMillis: Long,

    // Menyimpan waktu selesai sesi belajar dalam format milidetik (epoch time).
    // Ini penting untuk melacak kapan sesi berakhir dan menghitung durasi aktual.
    @ColumnInfo(name = "end_time_millis")
    val endTimeMillis: Long,

    // Durasi fokus yang direncanakan atau yang berhasil diselesaikan pengguna dalam menit.
    @ColumnInfo(name = "focus_duration_minutes")
    val focusDurationMinutes: Int,

    // Durasi istirahat yang diambil setelah sesi fokus ini (jika ada) dalam menit.
    @ColumnInfo(name = "break_duration_minutes")
    val breakDurationMinutes: Int,

    // Mata pelajaran atau topik spesifik dari sesi belajar.
    // Dibuat nullable (String?) karena pengguna mungkin tidak selalu mengisinya.
    @ColumnInfo(name = "subject")
    val subject: String? = null,

    // Hasil atau kualitas dari sesi belajar, misalnya: "Focused", "Distracted", "Completed".
    // Dibuat nullable (String?) karena mungkin tidak selalu diisi atau relevan.
    @ColumnInfo(name = "session_outcome")
    val sessionOutcome: String? = null,

    // Menyimpan tanggal sesi belajar dalam format milidetik (epoch time).
    // Biasanya ini adalah timestamp yang dinormalisasi ke awal hari (00:00:00) dari startTimeMillis.
    // Berguna untuk query data sesi berdasarkan rentang tanggal tertentu.
    @ColumnInfo(name = "date_millis")
    val dateMillis: Long,

    // BARIS YANG DITAMBAHKAN:
    @ColumnInfo(name = "mood")
    var mood: String? = null // Bisa var jika akan diubah, String? jika bisa null
)