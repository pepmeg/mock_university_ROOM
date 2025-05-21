package com.example.a12.pages

import android.app.Application
import com.example.a12.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        deleteDatabase("mock_university.db")
        AppDatabase.clearInstance()
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(applicationContext).testDao().seedAll()
        }
    }
}