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
        TestResultEntity::class,
        UserAnswerEntity::class
    ],
    version = 1
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
                    "database_init.sql"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        loadInitialData(context, db)
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private fun loadInitialData(context: Context, db: SupportSQLiteDatabase) {
            val inputStream = context.assets.open("database_init.sql")
            val reader = BufferedReader(InputStreamReader(inputStream))

            db.beginTransaction()
            try {
                var line: String? = reader.readLine()
                while (line != null) {
                    val query = line.trim()
                    if (query.isNotEmpty()) {
                        db.execSQL(query)
                    }
                    line = reader.readLine()
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
                reader.close() // Close the reader to release resources
                inputStream.close() //Close the inputstream
            }
        }
    }
}