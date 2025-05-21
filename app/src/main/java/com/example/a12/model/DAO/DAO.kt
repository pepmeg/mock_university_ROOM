package com.example.a12.model.DAO

import androidx.room.*
import com.example.a12.model.entities.*

@Dao
interface TestDao {
    @Query("SELECT * FROM tests")
    suspend fun getAllTests(): List<TestEntity>

    @Query("SELECT * FROM tests WHERE test_id = :testId")
    suspend fun getTestById(testId: Long): TestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Query("DELETE FROM tests WHERE test_id = :testId")
    suspend fun deleteTest(testId: Long)

    @Query("SELECT * FROM questions WHERE test_id = :testId ORDER BY order_number ASC")
    suspend fun getQuestions(testId: Long): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Query("DELETE FROM questions WHERE test_id = :testId")
    suspend fun deleteQuestionsForTest(testId: Long)

    @Query("SELECT * FROM answers WHERE question_id = :questionId")
    suspend fun getAnswersForQuestion(questionId: Long): List<AnswerEntity>

    @Query("SELECT test_name FROM tests WHERE test_id = :testId")
    suspend fun getTestName(testId: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity): Long

    @Query("DELETE FROM answers WHERE question_id = :questionId")
    suspend fun deleteAnswersForQuestion(questionId: Long)

    @Query("SELECT * FROM test_results WHERE test_id = :testId ORDER BY result_id DESC")
    suspend fun getTestResults(testId: Long): List<TestResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(result: TestResultEntity): Long

    @Update
    suspend fun updateTestResult(result: TestResultEntity)

    @Query("DELETE FROM test_results WHERE test_id = :testId")
    suspend fun deleteTestResults(testId: Long)

    @Query("SELECT * FROM user_answers WHERE result_id = :resultId AND question_id = :questionId")
    suspend fun getUserAnswer(resultId: Long, questionId: Long): UserAnswerEntity?

    @Query("SELECT duration_minutes FROM tests WHERE test_id = :testId")
    suspend fun getTestDurationMinutes(testId: Long): Int

    @Query("SELECT test_id FROM test_results WHERE result_id = :resultId")
    suspend fun getTestIdByResult(resultId: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(userAnswer: UserAnswerEntity): Long

    @Query("UPDATE test_results SET remaining_seconds = :remainingSeconds WHERE result_id = :resultId")
    suspend fun updateRemainingTime(resultId: Long, remainingSeconds: Int)

    @Update
    suspend fun updateUserAnswer(userAnswer: UserAnswerEntity)

    @Query("DELETE FROM user_answers WHERE result_id = :resultId")
    suspend fun deleteUserAnswers(resultId: Long)

    @Transaction
    @Query(
        """
        SELECT t.*, 
               COUNT(q.question_id) AS total_questions,
               tr.status AS last_status,
               tr.result_id AS last_result_id
        FROM tests t
        LEFT JOIN questions q ON q.test_id = t.test_id
        LEFT JOIN (
            SELECT test_id, MAX(result_id) AS last_result_id 
            FROM test_results 
            GROUP BY test_id
        ) latest ON latest.test_id = t.test_id
        LEFT JOIN test_results tr ON tr.result_id = latest.last_result_id
        GROUP BY t.test_id
    """
    )
    suspend fun getAllTestItems(): List<TestWithStats>

    data class TestWithStats(
        @Embedded val test: TestEntity,
        @ColumnInfo(name = "total_questions") val totalQuestions: Int,
        @ColumnInfo(name = "last_status") val lastStatus: String?,
        @ColumnInfo(name = "last_result_id") val lastResultId: Long?
    )

    @Query(
        """
        SELECT 
            SUM(CASE WHEN ua.is_correct = 1 THEN 1 ELSE 0 END) AS correct,
            COUNT(*) AS total 
        FROM user_answers ua
        WHERE ua.result_id = :resultId
    """
    )
    suspend fun getResultStats(resultId: Long): ResultStats

    data class ResultStats(
        @ColumnInfo(name = "correct") val correctAnswers: Int,
        @ColumnInfo(name = "total") val totalAnswers: Int
    )

    @Transaction
    suspend fun startTestSession(testId: Long): Long {
        val now = System.currentTimeMillis() / 1000
        val result = TestResultEntity(
            testId = testId,
            status = "in_progress",
            currentQuestionOrder = 1,
            remainingSeconds = null,
            finishedAt = null,
            correctPercentage = 0.0,
            createdAt = now
        )
        return insertTestResult(result)
    }

    @Transaction
    suspend fun finishTestSession(resultId: Long, remainingSeconds: Int?) {
        val result = getTestResult(resultId) ?: return
        val updatedResult = result.copy(
            status = "completed",
            finishedAt = System.currentTimeMillis(),
            remainingSeconds = remainingSeconds
        )
        updateTestResult(updatedResult)
    }

    @Transaction
    suspend fun saveUserAnswer(
        resultId: Long,
        questionId: Long,
        answerId: Long?,
        freeText: String?,
        isCorrect: Boolean
    ) {
        val existing = getUserAnswer(resultId, questionId)
        val userAnswer = existing?.copy(
            answerId = answerId,
            freeTextAnswer = freeText,
            isCorrect = isCorrect
        ) ?: UserAnswerEntity(
            userAnswerId = 0,
            resultId = resultId,
            questionId = questionId,
            answerId = answerId,
            freeTextAnswer = freeText,
            isCorrect = isCorrect
        )

        if (existing != null) {
            updateUserAnswer(userAnswer)
        } else {
            insertUserAnswer(userAnswer)
        }
    }

    @Query("SELECT * FROM test_results WHERE result_id = :resultId")
    suspend fun getTestResult(resultId: Long): TestResultEntity?

    @Query("SELECT * FROM answers WHERE question_id = :questionId")
    suspend fun getAnswers(questionId: Long): List<AnswerEntity>

    @Query("SELECT duration_minutes FROM tests WHERE test_id = :testId")
    suspend fun getTestDuration(testId: Long): Int

    @Query("SELECT COUNT(*) FROM questions WHERE test_id = :testId")
    suspend fun getQuestionCount(testId: Long): Int

    @Query("SELECT remaining_seconds FROM test_results WHERE result_id = :resultId")
    suspend fun getRemainingSeconds(resultId: Long): Int?

    @Query("UPDATE test_results SET remaining_seconds = :seconds WHERE result_id = :resultId")
    suspend fun updateRemainingSeconds(resultId: Long, seconds: Int)

    @Query(
        """
        SELECT * FROM test_results 
        WHERE status = 'in_progress'
        ORDER BY result_id DESC 
        LIMIT 1
    """
    )
    suspend fun getLastInProgressResult(): TestResultEntity?


    @Transaction
    @Query(
        """
    SELECT 
      t.*,
      COUNT(q.question_id)             AS total_questions,
      tr.status                        AS last_status,
      tr.result_id                     AS last_result_id
    FROM tests t
    LEFT JOIN questions q ON q.test_id = t.test_id
    LEFT JOIN (
      SELECT test_id, MAX(result_id) AS last_result_id 
      FROM test_results 
      GROUP BY test_id
    ) latest ON latest.test_id = t.test_id
    LEFT JOIN test_results tr ON tr.result_id = latest.last_result_id
    WHERE t.test_id = :testId
    GROUP BY t.test_id
"""
    )
    suspend fun getTestWithStatsById(testId: Long): TestWithStats?

    @Transaction
    suspend fun seedAll() {
        insertTest(TestEntity(testName = "Java Core", description = null, durationMinutes = 20))
        insertTest(TestEntity(testName = "Основы C++", description = null, durationMinutes = 10))
        insertTest(TestEntity(testName = "React JS", description = null, durationMinutes = 10))

        listOf(
            QuestionEntity(testId = 1,questionText ="Как объявить класс в коде?",questionType = "single",minAnswers = 1,maxAnswers = 1,orderNumber = 1),
            QuestionEntity(testId = 1,questionText ="Где правильно создан массив?",questionType = "single",minAnswers = 1,maxAnswers = 1,orderNumber = 2),
            QuestionEntity(testId = 1,questionText ="Какой класс отвечает за получение информации от пользователя?",questionType = "single",minAnswers = 1,maxAnswers = 1,orderNumber = 3),
            QuestionEntity(testId = 1,questionText ="Какие математические операции есть в Java?",questionType = "single",minAnswers = 1,maxAnswers = 1,orderNumber = 4)
        ).forEach { insertQuestion(it) }

        listOf(
            AnswerEntity(questionId = 1,answerText = "class MyClass {}",isCorrect = true),
            AnswerEntity(questionId = 1,answerText = "new class MyClass {}", isCorrect = false),
            AnswerEntity(questionId = 1,answerText = "select * from class MyClass {}", isCorrect = false),
            AnswerEntity(questionId = 1,answerText = "MyClass extends class {}",isCorrect = false),
            AnswerEntity(questionId = 2,answerText = "int a[] = {1, 2, 3, 4, 5};", isCorrect = false),
            AnswerEntity(questionId = 2,answerText = "int[] a = new int[] {1, 2, 3, 4, 5};", isCorrect = true),
            AnswerEntity(questionId = 2,answerText = "int[] a = new int {1, 2, 3, 4, 5};", isCorrect = false),
            AnswerEntity(questionId = 2,answerText = "int[] a = int[] {1, 2, 3, 4, 5};", isCorrect = false),
            AnswerEntity(questionId = 3,answerText = "int[] a = int[] {1, 2, 3, 4, 5};", isCorrect = false),
        ).forEach { insertAnswer(it) }

    }
}