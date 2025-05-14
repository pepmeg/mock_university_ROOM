package com.example.a12.pages

import android.app.Application
import com.example.a12.model.copyDatabaseFromAssets

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        copyDatabaseFromAssets(this)
    }
}