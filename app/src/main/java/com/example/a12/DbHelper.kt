package com.example.a12

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.a12.model.Question
import com.example.a12.model.Answer
import com.example.a12.model.TestItem
import java.io.File
import java.io.FileOutputStream

class DbHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mock_university.db"
        private const val DATABASE_VERSION = 1
    }

    init {
        logTables()
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA foreign_keys=ON;")
    }
    /**
     * Копирует предзаполненную БД из assets при первом запуске,
     * удаляя при этом старые файлы *.db, *.db-wal и *.db-shm.
     */

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
        return 0
    }
    fun getUserAnswer(questionId: Int): Int? {
        val db = readableDatabase
        val cursor = db.query(
            "user_answers",
            arrayOf("answer_id"),
            "question_id = ?",
            arrayOf(questionId.toString()),
            null, null, null
        )
        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) else null
        }
    }

    fun saveUserAnswer(questionId: Int, answerId: Int, resultId: Int = 1) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("question_id", questionId)
            put("answer_id", answerId)
            put("result_id", resultId)
        }
        val updated = db.update(
            "user_answers", values,
            "question_id = ?", arrayOf(questionId.toString())
        )
        if (updated == 0) {
            db.insert("user_answers", null, values)
        }
    }

    fun getAllTestItems(): List<TestItem> {
        val db = readableDatabase
        val sql = """
        WITH latest_results AS (
            SELECT test_id, MAX(result_id) AS result_id
            FROM test_results
            GROUP BY test_id
        )
        SELECT
          t.test_id,
          t.test_name,
          t.duration_minutes,
          COUNT(q.question_id) AS total_cnt,
          COALESCE(ua.answered_cnt, 0) AS answered_cnt
        FROM tests t
        -- посчитаем общее число вопросов
        LEFT JOIN questions q ON q.test_id = t.test_id
        -- найдем последнюю сессию, если была
        LEFT JOIN latest_results lr ON lr.test_id = t.test_id
        -- посчитаем уникальные ответы в этой сессии
        LEFT JOIN (
            SELECT result_id, COUNT(DISTINCT question_id) AS answered_cnt
            FROM user_answers
            GROUP BY result_id
        ) ua ON ua.result_id = lr.result_id
        GROUP BY t.test_id, t.test_name, t.duration_minutes, ua.answered_cnt
    """.trimIndent()

        val cursor = db.rawQuery(sql, null)
        val list = mutableListOf<TestItem>()
        cursor.use {
            while (it.moveToNext()) {
                list += TestItem(
                    id               = it.getInt(it.getColumnIndexOrThrow("test_id")),
                    name             = it.getString(it.getColumnIndexOrThrow("test_name")),
                    durationMinutes  = it.getInt(it.getColumnIndexOrThrow("duration_minutes")),
                    questionsCount   = it.getInt(it.getColumnIndexOrThrow("total_cnt")),
                    answeredCount    = it.getInt(it.getColumnIndexOrThrow("answered_cnt"))
                )
            }
        }

        return list
    }

    fun finishTestSession(resultId: Long, remainingSeconds: Int?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("end_time", System.currentTimeMillis())
            put("remaining_seconds", remainingSeconds)
        }
        db.update("test_results", values, "result_id = ?", arrayOf(resultId.toString()))
    }

    fun startTestSession(testId: Int): Long {
        val values = ContentValues().apply {
            put("test_id", testId)
            put("status", "in_progress")
            put("current_question_order", 1)
        }
        return writableDatabase.insert("test_results", null, values)
    }

    /** Завершает сессию, ставит статус completed и сохраняет время */
    fun finishTestSession(resultId: Long) {
        val values = ContentValues().apply {
            put("status", "completed")
            put("finished_at", System.currentTimeMillis())
        }
        writableDatabase.update(
            "test_results", values,
            "result_id = ?", arrayOf(resultId.toString())
        )
    }

    /**
     * Сохраняет или обновляет ответ пользователя:
     * если по (resultId, questionId) запись есть — обновляем, иначе — вставляем
     */
    fun saveUserAnswer(
        resultId: Long,
        questionId: Int,
        answerId: Int?,
        freeTextAnswer: String? = null,
        isCorrect: Int = 0
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("result_id", resultId)
            put("question_id", questionId)
            put("answer_id", answerId)
            put("free_text_answer", freeTextAnswer)
            put("is_correct", isCorrect)
        }
        val updated = db.update(
            "user_answers", values,
            "result_id = ? AND question_id = ?",
            arrayOf(resultId.toString(), questionId.toString())
        )
        if (updated == 0) {
            db.insert("user_answers", null, values)
        }
    }

    /** Возвращает сохранённый ответ (answer_id) или null */
    fun getUserAnswer(resultId: Long, questionId: Int): Int? {
        val cursor = readableDatabase.query(
            "user_answers",
            arrayOf("answer_id"),
            "result_id = ? AND question_id = ?",
            arrayOf(resultId.toString(), questionId.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else null
        }
    }

    fun getCorrectPercentage(resultId: Long): Double {
        readableDatabase.rawQuery(
            "SELECT correct_percentage FROM test_results WHERE result_id = ?",
            arrayOf(resultId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getDouble(cursor.getColumnIndexOrThrow("correct_percentage"))
            }
        }
        return 0.0
    }

    /**
     * Возвращает пару (число правильных ответов, общее число вопросов) для данной сессии.
     */
    fun getCorrectAndTotalCounts(resultId: Long): Pair<Int, Int> {
        // общий запрос считает и правильно отвеченные, и все ответы
        val sql = """
        SELECT 
          SUM(CASE WHEN ua.is_correct=1 THEN 1 ELSE 0 END) AS correct_cnt,
          COUNT(*) AS total_cnt
        FROM user_answers ua
        WHERE ua.result_id = ?
    """.trimIndent()

        readableDatabase.rawQuery(sql, arrayOf(resultId.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                val correct = cursor.getInt(cursor.getColumnIndexOrThrow("correct_cnt"))
                val total   = cursor.getInt(cursor.getColumnIndexOrThrow("total_cnt"))
                return Pair(correct, total)
            }
        }
        return Pair(0, 0)
    }

    fun getUserAnswerIsCorrect(resultId: Long, questionId: Int): Boolean {
        readableDatabase.rawQuery(
            "SELECT is_correct FROM user_answers WHERE result_id=? AND question_id=?",
            arrayOf(resultId.toString(), questionId.toString())
        ).use { c ->
            if (c.moveToFirst()) {
                return c.getInt(c.getColumnIndexOrThrow("is_correct")) == 1
            }
        }
        return false
    }

    fun getTotalQuestionCount(testId: Int): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM questions WHERE test_id = ?",
            arrayOf(testId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return 0
    }

}