package com.example.moodooro

import android.app.Application
import com.example.moodooro.data.local.MoodooroDatabase

class MyApplication : Application() {

    // Instance database dibuat menggunakan lazy delegate
    // agar hanya dibuat saat pertama kali diakses.
    val database: MoodooroDatabase by lazy { MoodooroDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        // Anda bisa melakukan inisialisasi lain di sini jika diperlukan.
    }
}