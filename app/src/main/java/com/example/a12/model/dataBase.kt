package com.example.a12.model

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.entities.*

@Database(
    entities = [
        TestEntity::class,
        QuestionEntity::class,
        AnswerEntity::class,
        TestResultEntity::class,
        UserAnswerEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun testDao(): TestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tests ADD COLUMN description TEXT NOT NULL DEFAULT ''")

                database.execSQL("UPDATE tests SET description = 'Этот тест проверяет базовые знания языка программирования Java, включая синтаксис, объектно-ориентированное программирование, работу с коллекциями и обработку исключений. Подходит для начинающих и тех, кто хочет освежить свои знания.' WHERE test_id = 1")
                database.execSQL("UPDATE tests SET description = 'Тест предназначен для оценки понимания основ языка C++, таких как указатели, управление памятью, классы и шаблоны. Отлично подходит для студентов и разработчиков, стремящихся укрепить свои навыки в C++.' WHERE test_id = 2")
                database.execSQL("UPDATE tests SET description = 'Этот тест фокусируется на ключевых концепциях библиотеки React, включая компоненты, состояние, свойства и жизненный цикл компонентов. Рекомендуется для фронтенд-разработчиков, желающих проверить свои знания в React.' WHERE test_id = 3")
            }
        }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mock_university.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}