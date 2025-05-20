package com.example.a12.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.entities.*
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(
    entities = [
        TestEntity::class,
        QuestionEntity::class,
        AnswerEntity::class,
        DemoEntity::class,
        TestMetadataEntity::class,
        TestResultEntity::class,
        UserAnswerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mock_university.db"
                )
                    // при несовпадении схемы — удалять и пересоздавать БД (для dev-стадии)
                    .fallbackToDestructiveMigration()
                    // при первом создании выполнять скрипт из assets/database_init.sql
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            loadInitialData(context, db)
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private fun loadInitialData(
            context: Context,
            db: SupportSQLiteDatabase
        ) {
            val assetManager = context.assets
            assetManager.open("database_init.sql").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    db.beginTransaction()
                    try {
                        var sqlLine: String?
                        while (reader.readLine().also { sqlLine = it } != null) {
                            val stmt = sqlLine!!.trim()
                            if (stmt.isNotEmpty()) {
                                db.execSQL(stmt)
                            }
                        }
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            }
        }
    }
}