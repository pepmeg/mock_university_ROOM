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
        val sql = """
        WITH latest AS (
          SELECT test_id, MAX(result_id) AS result_id
          FROM test_results
          GROUP BY test_id
        ), ua AS (
          SELECT result_id, COUNT(DISTINCT question_id) AS answered_cnt
          FROM user_answers GROUP BY result_id
        )
        SELECT
          t.test_id,
          COALESCE(t.test_name, '')               AS test_name,
          COALESCE(t.duration_minutes, 0)         AS duration_minutes,
          COUNT(q.question_id)                    AS total_cnt,
          COALESCE(ua.answered_cnt, 0)            AS answered_cnt,
          COALESCE(tr.status, 'in_progress')      AS status,
          COALESCE(tr.remaining_seconds, t.duration_minutes * 60) 
                                                  AS remaining_sec
        FROM tests t
          LEFT JOIN questions q     ON q.test_id = t.test_id
          LEFT JOIN latest l        ON l.test_id = t.test_id
          LEFT JOIN test_results tr ON tr.result_id = l.result_id
          LEFT JOIN ua              ON ua.result_id = l.result_id
        GROUP BY
          t.test_id, t.test_name, t.duration_minutes,
          ua.answered_cnt, tr.status, tr.remaining_seconds
    """.trimIndent()

        val out = mutableListOf<TestItem>()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) {
                // безопасно читаем
                val id     = c.getInt(c.getColumnIndexOrThrow("test_id"))
                val name   = c.getString(c.getColumnIndexOrThrow("test_name"))
                val durMin = c.getInt(c.getColumnIndexOrThrow("duration_minutes"))
                val total  = c.getInt(c.getColumnIndexOrThrow("total_cnt"))
                val answered = c.getInt(c.getColumnIndexOrThrow("answered_cnt"))
                val status  = c.getString(c.getColumnIndexOrThrow("status"))
                val remSec  = c.getLong(c.getColumnIndexOrThrow("remaining_sec"))

                // определяем иконку по имени
                val icon = when {
                    name.contains("Java",  ignoreCase = true) -> "java_logo"
                    name.contains("C++",   ignoreCase = true) -> "c_logo"
                    name.contains("React", ignoreCase = true) -> "react_logo"
                    else                                     -> "default_logo"
                }

                out += TestItem(
                    id               = id,
                    name             = name,
                    durationMinutes  = durMin,
                    questionsCount   = total,
                    answeredCount    = answered,
                    remainingSeconds = remSec,
                    status           = status,
                    iconResName      = icon
                )
            }
        }
        return out
    }

    /** Завершает сессию, ставит статус completed и сохраняет время */
    fun finishTestSession(resultId: Long, remainingSeconds: Int?) {
        val values = ContentValues().apply {
            put("status", "completed")
            put("finished_at", System.currentTimeMillis())
            if (remainingSeconds != null) {
                put("remaining_seconds", remainingSeconds)
            }
        }
        writableDatabase.update(
            "test_results", values,
            "result_id = ?", arrayOf(resultId.toString())
        )
    }

    fun startTestSession(testId: Int): Long {
        val v = ContentValues().apply {
            put("test_id", testId)
            put("status", "in_progress")
            put("current_question_order", 1)
            putNull("remaining_seconds")
        }
        return writableDatabase.insert("test_results", null, v)
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

    fun getInProgressTestItems(): List<TestItem> {
        val sql = """
      WITH latest AS (
        SELECT test_id, MAX(result_id) AS result_id
        FROM test_results
        GROUP BY test_id
      ), answered AS (
        SELECT result_id, COUNT(DISTINCT question_id) AS answered_cnt
        FROM user_answers
        GROUP BY result_id
      )
      SELECT
        t.test_id,
        COALESCE(t.test_name, '')           AS test_name,
        COALESCE(t.duration_minutes,   0)   AS duration_minutes,
        COUNT(q.question_id)                AS total_cnt,
        COALESCE(a.answered_cnt, 0)         AS answered_cnt,
        COALESCE(tr.remaining_seconds, t.duration_minutes*60) AS remaining_sec,
        tr.status                           AS status
      FROM tests t
        LEFT JOIN questions q     ON q.test_id     = t.test_id
        LEFT JOIN latest l        ON l.test_id     = t.test_id
        LEFT JOIN test_results tr ON tr.result_id  = l.result_id
        LEFT JOIN answered a      ON a.result_id   = l.result_id
      WHERE tr.status = 'in_progress'
      GROUP BY
        t.test_id, t.test_name, t.duration_minutes,
        a.answered_cnt, tr.remaining_seconds, tr.status
    """.trimIndent()

        val out = mutableListOf<TestItem>()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) {
                val id      = c.getInt(c.getColumnIndexOrThrow("test_id"))
                val name    = c.getString(c.getColumnIndexOrThrow("test_name"))
                val durMin  = c.getInt(c.getColumnIndexOrThrow("duration_minutes"))
                val total   = c.getInt(c.getColumnIndexOrThrow("total_cnt"))
                val answered= c.getInt(c.getColumnIndexOrThrow("answered_cnt"))
                val remSec  = c.getLong(c.getColumnIndexOrThrow("remaining_sec"))
                val status  = c.getString(c.getColumnIndexOrThrow("status"))

                // Определяем иконку по названию теста
                val iconRes = when {
                    name.contains("Java",  ignoreCase = true) -> "java_logo"
                    name.contains("C++",   ignoreCase = true) -> "c_logo"
                    name.contains("React", ignoreCase = true) -> "react_logo"
                    else                                     -> "default_logo"
                }

                out += TestItem(
                    id               = id,
                    name             = name,
                    durationMinutes  = durMin,
                    questionsCount   = total,
                    answeredCount    = answered,
                    remainingSeconds = remSec,
                    status           = status,
                    iconResName      = iconRes
                )
            }
        }
        return out
    }

}