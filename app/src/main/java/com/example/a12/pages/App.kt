package com.example.a12.pages

import android.app.Application
import com.example.a12.model.AppDatabase
import com.example.a12.model.DatabaseInitializer

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer(this).initialize()
    }
}
