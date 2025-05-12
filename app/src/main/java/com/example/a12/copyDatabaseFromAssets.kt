package com.example.a12

import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun copyDatabaseFromAssets(context: Context) {
    val dbName = "mock_university.db"
    val dbFile = context.getDatabasePath(dbName)

    // 1) Удаляем старую БД и вспомогательные файлы
    if (dbFile.exists()) {
        dbFile.delete()
    }
    File(dbFile.absolutePath + "-wal").delete()
    File(dbFile.absolutePath + "-shm").delete()

    // 2) Копируем новую БД из assets
    dbFile.parentFile?.mkdirs()
    context.assets.open(dbName).use { input ->
        FileOutputStream(dbFile).use { output ->
            input.copyTo(output)
        }
    }
}