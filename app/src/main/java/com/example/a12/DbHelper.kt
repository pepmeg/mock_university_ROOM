package com.example.a12

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.a12.model.Question
import com.example.a12.model.Answer
import java.io.File
import java.io.FileOutputStream

class DbHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mock_university.db"
        private const val DATABASE_VERSION = 1
    }

    init {
        copyDatabaseIfNeeded()
        logTables()
    }

    /**
     * Копирует предзаполненную БД из assets при первом запуске,
     * удаляя при этом старые файлы *.db, *.db-wal и *.db-shm.
     */
    private fun copyDatabaseIfNeeded() {
        val dbFile = context.applicationContext.getDatabasePath(DATABASE_NAME)

        // Удаляем старую БД и WAL/SHM-файлы (для отладки)
        context.deleteDatabase(DATABASE_NAME)
        File(dbFile.absolutePath + "-shm").delete()
        File(dbFile.absolutePath + "-wal").delete()

        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            context.assets.open(DATABASE_NAME).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("DbHelper", "Database copied from assets")
        } else {
            Log.d("DbHelper", "Database already exists, skipping copy")
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // No-op: схема создаётся в mock_university.db
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Миграции, если понадобится: можно удалять старую БД и вызывать copyDatabaseIfNeeded()
    }
    /**
     * Возвращает имя теста по его ID.
     */
    fun getTestName(testId: Int): String {
        readableDatabase.rawQuery(
            "SELECT test_name FROM tests WHERE test_id = ?",
            arrayOf(testId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("test_name"))
            }
        }
        return ""  // если тест не найден
    }

    /**
     * Возвращает список вопросов для заданного testId, упорядоченный по order_number.
     */
    fun getQuestions(testId: Int): List<Question> {
        val db = readableDatabase
        val cursor = db.query(
            "questions",
            arrayOf("question_id", "question_text", "question_type"),
            "test_id = ?",
            arrayOf(testId.toString()),
            null, null,
            "order_number ASC"
        )

        val questions = mutableListOf<Question>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("question_id"))
                val text = cursor.getString(cursor.getColumnIndexOrThrow("question_text"))
                val type = cursor.getString(cursor.getColumnIndexOrThrow("question_type"))
                questions.add(Question(id, text, type))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return questions
    }

    /**
     * Возвращает список ответов для заданного questionId.
     */
    fun getAnswers(questionId: Int): List<Answer> {
        val db = readableDatabase
        val cursor = db.query(
            "answers",
            arrayOf("answer_id", "answer_text", "is_correct"),
            "question_id = ?",
            arrayOf(questionId.toString()),
            null, null, null
        )

        val answers = mutableListOf<Answer>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("answer_id"))
                val text = cursor.getString(cursor.getColumnIndexOrThrow("answer_text"))
                val isCorrect = cursor.getInt(cursor.getColumnIndexOrThrow("is_correct")) == 1
                answers.add(Answer(id, text, isCorrect))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return answers
    }

    /**
     * Логирует все таблицы из sqlite_master для отладки.
     */
    private fun logTables() {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table'",
            null
        )
        while (cursor.moveToNext()) {
            Log.d("DbHelper", "Table in DB: ${cursor.getString(0)}")
        }
        cursor.close()
    }

    /**
     * Возвращает длительность теста в минутах.
     */
    fun getTestDurationMinutes(testId: Int): Int {
        readableDatabase.rawQuery(
            "SELECT duration_minutes FROM tests WHERE test_id = ?",
            arrayOf(testId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes"))
            }
        }
        // значение по умолчанию, если тест не найден
        return 0
    }
}