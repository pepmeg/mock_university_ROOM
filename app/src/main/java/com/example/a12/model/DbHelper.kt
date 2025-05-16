package com.example.a12.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

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

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun getTestName(testId: Int): String {
        readableDatabase.rawQuery(
            "SELECT test_name FROM tests WHERE test_id = ?",
            arrayOf(testId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("test_name"))
            }
        }
        return ""
    }

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

    fun getInitialMillis(resultId: Long): Long {
        readableDatabase.rawQuery(
            "SELECT remaining_seconds FROM test_results WHERE result_id = ?",
            arrayOf(resultId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val sec = cursor.getInt(cursor.getColumnIndexOrThrow("remaining_seconds"))
                if (sec > 0) return sec * 1_000L
            }
        }
        val minutes = getTestDurationMinutes(getTestIdByResult(resultId))
        return minutes * 60_000L
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
    COALESCE(tr.remaining_seconds, t.duration_minutes * 60) AS remaining_sec,
    tr.finished_at                          AS finished_at     
  FROM tests t
    LEFT JOIN questions q     ON q.test_id = t.test_id
    LEFT JOIN latest l        ON l.test_id = t.test_id
    LEFT JOIN test_results tr ON tr.result_id = l.result_id
    LEFT JOIN ua              ON ua.result_id = l.result_id
  GROUP BY
    t.test_id, t.test_name, t.duration_minutes,
    ua.answered_cnt, tr.status, tr.remaining_seconds, tr.finished_at
""".trimIndent()

        val out = mutableListOf<TestItem>()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) {
                val id     = c.getInt(c.getColumnIndexOrThrow("test_id"))
                val name   = c.getString(c.getColumnIndexOrThrow("test_name"))
                val durMin = c.getInt(c.getColumnIndexOrThrow("duration_minutes"))
                val total  = c.getInt(c.getColumnIndexOrThrow("total_cnt"))
                val answered = c.getInt(c.getColumnIndexOrThrow("answered_cnt"))
                val status  = c.getString(c.getColumnIndexOrThrow("status"))
                val remSec  = c.getLong(c.getColumnIndexOrThrow("remaining_sec"))
                val finishedAt = c.getLong(c.getColumnIndexOrThrow("finished_at"))

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
                    iconResName      = icon,
                    finishedAt       = if (c.isNull(c.getColumnIndexOrThrow("finished_at"))) null
                    else finishedAt
                )
            }
        }
        return out
    }

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

    fun saveUserAnswer(
        resultId: Long,
        questionId: Int,
        answerId: Int?,
        freeTextAnswer: String? = null,
        isCorrect: Int = 0,
        remainingSeconds: Int? = null
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
        if (remainingSeconds != null) {
            updateRemainingTime(resultId, remainingSeconds)
        }
    }

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

    fun getCorrectAndTotalCounts(resultId: Long): Pair<Int, Int> {
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

    fun updateRemainingTime(resultId: Long, remainingSeconds: Int) {
        val values = ContentValues().apply {
            put("remaining_seconds", remainingSeconds)
        }
        writableDatabase.update(
            "test_results",
            values,
            "result_id = ?",
            arrayOf(resultId.toString())
        )
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
        COALESCE(t.test_name, '')                              AS test_name,
        COALESCE(t.duration_minutes,   0)                      AS duration_minutes,
        COUNT(q.question_id)                                   AS total_cnt,
        COALESCE(a.answered_cnt, 0)                            AS answered_cnt,
        COALESCE(tr.remaining_seconds, t.duration_minutes * 60) AS remaining_sec,
        tr.status                                              AS status,
        tr.finished_at                                         AS finished_at
      FROM tests t
        LEFT JOIN questions q     ON q.test_id     = t.test_id
        LEFT JOIN latest l        ON l.test_id     = t.test_id
        LEFT JOIN test_results tr ON tr.result_id  = l.result_id
        LEFT JOIN answered a      ON a.result_id   = l.result_id
      WHERE tr.status = 'in_progress'
      GROUP BY t.test_id, t.test_name, t.duration_minutes,
               a.answered_cnt, tr.remaining_seconds, tr.status, tr.finished_at
    """.trimIndent()

        val out = mutableListOf<TestItem>()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) {
                val id         = c.getInt(c.getColumnIndexOrThrow("test_id"))
                val name       = c.getString(c.getColumnIndexOrThrow("test_name"))
                val durMin     = c.getInt(c.getColumnIndexOrThrow("duration_minutes"))
                val total      = c.getInt(c.getColumnIndexOrThrow("total_cnt"))
                val answered   = c.getInt(c.getColumnIndexOrThrow("answered_cnt"))
                val remSec     = c.getLong(c.getColumnIndexOrThrow("remaining_sec"))
                val status     = c.getString(c.getColumnIndexOrThrow("status"))
                val finishedAt = if (c.isNull(c.getColumnIndexOrThrow("finished_at")))
                    null
                else
                    c.getLong(c.getColumnIndexOrThrow("finished_at"))

                val iconRes = when {
                    name.contains("Java",  ignoreCase = true) -> "java_logo"
                    name.contains("C++",   ignoreCase = true) -> "c_logo"
                    name.contains("React", ignoreCase = true) -> "react_logo"
                    else                                     -> "java_logo"
                }

                out += TestItem(
                    id               = id,
                    name             = name,
                    durationMinutes  = durMin,
                    questionsCount   = total,
                    answeredCount    = answered,
                    remainingSeconds = remSec,
                    status           = status,
                    iconResName      = iconRes,
                    finishedAt       = finishedAt
                )
            }
        }
        return out
    }

    fun getCompletedTestItems(): List<TestItem> {
        val sql = """
  WITH latest AS (
    SELECT test_id, MAX(result_id) AS result_id
    FROM test_results
    GROUP BY test_id
  )
  SELECT
    t.test_id,
    t.test_name,
    t.duration_minutes,
    COUNT(q.question_id)                    AS total_cnt,
    COALESCE(ua.answered_cnt, 0)            AS answered_cnt,
    COALESCE(tr.remaining_seconds, t.duration_minutes*60) AS remaining_sec,
    tr.status,
    tr.finished_at                         AS finished_at
  FROM tests t
    LEFT JOIN questions q     ON q.test_id = t.test_id
    LEFT JOIN latest l        ON l.test_id = t.test_id
    LEFT JOIN test_results tr ON tr.result_id = l.result_id
    LEFT JOIN (
      SELECT result_id, COUNT(DISTINCT question_id) AS answered_cnt
      FROM user_answers
      GROUP BY result_id
    ) ua ON ua.result_id = l.result_id
  WHERE tr.status = 'completed'
  GROUP BY t.test_id, t.test_name, t.duration_minutes,
           ua.answered_cnt, tr.remaining_seconds, tr.status, tr.finished_at
    """.trimIndent()

        val out = mutableListOf<TestItem>()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) {
                val id    = c.getInt(c.getColumnIndexOrThrow("test_id"))
                val name  = c.getString(c.getColumnIndexOrThrow("test_name"))
                val dur   = c.getInt(c.getColumnIndexOrThrow("duration_minutes"))
                val total = c.getInt(c.getColumnIndexOrThrow("total_cnt"))
                val answ  = c.getInt(c.getColumnIndexOrThrow("answered_cnt"))
                val rem   = c.getLong(c.getColumnIndexOrThrow("remaining_sec"))
                val finishedAt = c.getLong(c.getColumnIndexOrThrow("finished_at"))

                val iconResName = when {
                    name.contains("Java",  ignoreCase = true) -> "java_logo"
                    name.contains("C++",   ignoreCase = true) -> "c_logo"
                    name.contains("React", ignoreCase = true) -> "react_logo"
                    else                                     -> "java_logo"
                }

                out += TestItem(
                    id               = id,
                    name             = name,
                    durationMinutes  = dur,
                    questionsCount   = total,
                    answeredCount    = answ,
                    remainingSeconds = rem,
                    status           = "completed",
                    iconResName      = iconResName,
                    finishedAt       = if (c.isNull(c.getColumnIndexOrThrow("finished_at"))) null
                    else finishedAt
                )
            }
        }
        return out
    }
    
    fun getLastResultForTest(testId: Int): Pair<Long, String>? {
        val sql = """
        SELECT result_id, status
        FROM test_results
        WHERE test_id = ?
        ORDER BY result_id DESC
        LIMIT 1
    """.trimIndent()

        readableDatabase.rawQuery(sql, arrayOf(testId.toString())).use { c ->
            if (c.moveToFirst()) {
                val id     = c.getLong(c.getColumnIndexOrThrow("result_id"))
                val status = c.getString(c.getColumnIndexOrThrow("status"))
                return id to status
            }
        }
        return null
    }

    fun getLastResultForTestForAnyTest(): Pair<Long, String>? {
        val sql = """
    SELECT result_id, status
      FROM test_results
     ORDER BY result_id DESC
     LIMIT 1
  """
        readableDatabase.rawQuery(sql, null).use { c ->
            if (c.moveToFirst()) {
                return c.getLong(0) to c.getString(1)
            }
        }
        return null
    }

    fun getTestIdByResult(resultId: Long): Int {
        readableDatabase.rawQuery(
            "SELECT test_id FROM test_results WHERE result_id = ?",
            arrayOf(resultId.toString())
        ).use {
            if (it.moveToFirst()) return it.getInt(0)
        }
        throw IllegalStateException("No test for result_id=$resultId")
    }

    fun getTestItemById(testId: Int): TestItem =
        getAllTestItems().first { it.id == testId }

    fun deleteAllResultsForTest(testId: Int) {
        writableDatabase.use { db ->
            db.delete(
                "user_answers",
                "result_id IN (SELECT result_id FROM test_results WHERE test_id = ?)",
                arrayOf(testId.toString())
            )
            db.delete(
                "test_results",
                "test_id = ?",
                arrayOf(testId.toString())
            )
        }
    }
}