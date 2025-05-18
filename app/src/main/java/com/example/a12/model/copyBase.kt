package com.example.a12.model

import android.content.Context
import androidx.room.Room
import java.io.File
import java.io.FileOutputStream

private const val DB_NAME = "mock_university.db"
@Volatile private var INSTANCE: AppDatabase? = null

fun copyDatabaseFromAssets(context: Context) {
    val dbFile = context.getDatabasePath(DB_NAME)
    if (!dbFile.exists()) {
        context.assets.open(DB_NAME).use { inp ->
            dbFile.parentFile?.mkdirs()
            FileOutputStream(dbFile).use { out ->
                inp.copyTo(out)
            }
        }
    }
    // 2) Построить Room поверх этого файла
    val inst = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        DB_NAME
    )
}