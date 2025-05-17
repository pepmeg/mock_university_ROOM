package com.example.a12.model

import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun copyDatabaseFromAssets(context: Context) {
    val dbName = "mock_university.db"
    val dbFile = context.getDatabasePath(dbName)

    if (dbFile.exists()) {
        dbFile.delete()
    }

    context.assets.open(dbName).use { input ->
        FileOutputStream(dbFile).use { output ->
            input.copyTo(output)
        }
    }
}