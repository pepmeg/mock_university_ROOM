package com.example.a12

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Проверяем, первый ли запуск
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        copyDatabaseFromAssets(this)
    }
}