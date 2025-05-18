package com.example.a12.model


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.a12.model.entities.*

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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mock_university.db"
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
}