package com.example.a12.pages

import android.app.Application
import com.example.a12.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    override fun onCreate() {
        super.onCreate()
//        deleteDatabase("mock_university.db")
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(applicationContext)
            val testDao = db.testDao()
            val testCount = testDao.getTestCount()
            if (testCount == 0) {
                testDao.seedAll()
            }
        }
    }
}