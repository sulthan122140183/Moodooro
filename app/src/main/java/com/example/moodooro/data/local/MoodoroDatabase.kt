package com.example.moodooro.data.local // Pastikan package name sesuai

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moodooro.data.local.dao.MoodEntryDao
import com.example.moodooro.data.local.dao.StudySessionDao
import com.example.moodooro.data.local.entity.MoodEntryEntity
import com.example.moodooro.data.local.entity.StudySessionEntity

/**
 * Kelas Database Room utama untuk aplikasi Moodoro.
 * Kelas ini bersifat abstrak dan akan diimplementasikan oleh Room.
 *
 * @property entities Array dari semua kelas entitas yang termasuk dalam database ini.
 * @property version Nomor versi database. Harus dinaikkan setiap kali skema database berubah.
 * @property exportSchema Jika true, Room akan mengekspor skema database ke dalam folder JSON.
 * Ini berguna untuk melacak riwayat versi skema dan untuk migrasi yang kompleks.
 * Untuk mengaktifkannya, Anda juga perlu mengkonfigurasi annotationProcessorOptions di build.gradle.
 */
@Database(
    entities = [StudySessionEntity::class, MoodEntryEntity::class], // Daftarkan semua entitas di sini
    version = 1, // Versi awal database. Naikkan jika ada perubahan skema.
    exportSchema = false // Set ke true jika Anda ingin mengekspor skema untuk migrasi
)
abstract class MoodoroDatabase : RoomDatabase() {

    /**
     * Metode abstrak untuk mendapatkan instance dari StudySessionDao.
     * Room akan mengimplementasikan metode ini.
     * @return Instance dari StudySessionDao.
     */
    abstract fun studySessionDao(): StudySessionDao

    /**
     * Metode abstrak untuk mendapatkan instance dari MoodEntryDao.
     * Room akan mengimplementasikan metode ini.
     * @return Instance dari MoodEntryDao.
     */
    abstract fun moodEntryDao(): MoodEntryDao

    companion object {
        // Anotasi @Volatile memastikan bahwa nilai dari INSTANCE selalu up-to-date
        // dan sama untuk semua thread eksekusi. Artinya, perubahan yang dibuat oleh satu thread
        // pada INSTANCE akan langsung terlihat oleh semua thread lainnya.
        @Volatile
        private var INSTANCE: MoodoroDatabase? = null

        /**
         * Mendapatkan instance singleton dari MoodoroDatabase.
         * Menggunakan pola singleton untuk mencegah beberapa instance database dibuka secara bersamaan,
         * yang bisa menjadi mahal dan menyebabkan masalah.
         *
         * @param context Context aplikasi, digunakan untuk mendapatkan path database.
         * @return Instance singleton dari MoodoroDatabase.
         */
        fun getDatabase(context: Context): MoodoroDatabase {
            // Jika INSTANCE sudah ada, kembalikan instance tersebut.
            // Jika belum, buat database baru.
            return INSTANCE ?: synchronized(this) {
                // synchronized block memastikan bahwa hanya satu thread yang dapat membuat instance database
                // pada satu waktu, mencegah race conditions.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodoroDatabase::class.java,
                    "moodoro_database" // Nama file database di perangkat
                )
                    // .fallbackToDestructiveMigration() // Hapus ini jika Anda mengimplementasikan migrasi yang benar
                    // .addCallback(roomCallback) // Jika Anda memiliki callback kustom
                    .build()
                INSTANCE = instance
                // Kembalikan instance yang baru dibuat
                instance
            }
        }

        // Contoh callback (opsional, jika Anda ingin melakukan sesuatu saat database dibuat/dibuka)
        /*
        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Lakukan sesuatu saat database pertama kali dibuat
                // Misalnya, mengisi data awal
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Lakukan sesuatu setiap kali database dibuka
            }
        }
        */
    }
}