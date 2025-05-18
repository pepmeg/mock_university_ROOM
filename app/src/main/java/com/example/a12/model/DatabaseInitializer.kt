package com.example.a12.model

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseInitializer(private val context: Context) {
    fun initialize() {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mock_university.db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                loadSQLScript(context, db)
            }
        }).build()
    }

    private fun loadSQLScript(context: Context, db: SupportSQLiteDatabase) {
        val inputStream = context.assets.open("database_init.sql")
        val sqlScript = inputStream.bufferedReader().use { it.readText() }

        db.beginTransaction()
        try {
            sqlScript.split(";").forEach { query ->
                if (query.trim().isNotEmpty()) {
                    db.execSQL(query.trim())
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}