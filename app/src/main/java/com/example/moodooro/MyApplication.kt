package com.example.moodooro

import android.app.Application
import com.example.moodooro.data.local.MoodooroDatabase
import kotlinx.coroutines.runBlocking

class MyApplication : Application() {

    // Instance database dibuat menggunakan lazy delegate
    // agar hanya dibuat saat pertama kali diakses.
    // Updated to use getSuspendingDatabase with runBlocking
    val database: MoodooroDatabase by lazy {
        runBlocking {
            MoodooroDatabase.getSuspendingDatabase(this@MyApplication)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Anda bisa melakukan inisialisasi lain di sini jika diperlukan.
    }
}