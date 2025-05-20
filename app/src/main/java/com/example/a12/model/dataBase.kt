package com.example.a12.model

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        /** Получить экземпляр БД, инициализировав если нужно */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        /** Строит сам Room‑экземпляр */
        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mock_university.db"
            )
                // при изменении схемы пересоздавать БД (dev‑режим)
                .fallbackToDestructiveMigration()
                // при первом создании — запуск сидинга
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // запускаем сидинг в bg‑корутине
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).testDao().seedAll()
                        }
                    }
                })
                .build()
    }
}