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
    // endregion

    // region Questions
    @Query("SELECT * FROM questions WHERE test_id = :testId ORDER BY order_number ASC")
    suspend fun getQuestionsForTest(testId: Long): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Query("DELETE FROM questions WHERE test_id = :testId")
    suspend fun deleteQuestionsForTest(testId: Long)
    // endregion

    // region Answers
    @Query("SELECT * FROM answers WHERE question_id = :questionId")
    suspend fun getAnswersForQuestion(questionId: Long): List<AnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity): Long

    @Query("DELETE FROM answers WHERE question_id = :questionId")
    suspend fun deleteAnswersForQuestion(questionId: Long)
    // endregion

    // region Test Results
    @Query("SELECT * FROM test_results WHERE test_id = :testId ORDER BY result_id DESC")
    suspend fun getTestResults(testId: Long): List<TestResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(result: TestResultEntity): Long

    @Update
    suspend fun updateTestResult(result: TestResultEntity)

    @Query("DELETE FROM test_results WHERE test_id = :testId")
    suspend fun deleteTestResults(testId: Long)
    // endregion

    // region User Answers
    @Query("SELECT * FROM user_answers WHERE result_id = :resultId")
    suspend fun getUserAnswers(resultId: Long): List<UserAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(userAnswer: UserAnswerEntity): Long

    @Update
    suspend fun updateUserAnswer(userAnswer: UserAnswerEntity)

    @Query("DELETE FROM user_answers WHERE result_id = :resultId")
    suspend fun deleteUserAnswers(resultId: Long)
    // endregion

    // region Complex Queries
    @Transaction
    @Query("""
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
    """)
    suspend fun getAllTestItems(): List<TestWithStats>

    data class TestWithStats(
        @Embedded val test: TestEntity,
        @ColumnInfo(name = "total_questions") val totalQuestions: Int,
        @ColumnInfo(name = "last_status") val lastStatus: String?,
        @ColumnInfo(name = "last_result_id") val lastResultId: Long?
    )

    @Query("""
        SELECT 
            SUM(CASE WHEN ua.is_correct = 1 THEN 1 ELSE 0 END) AS correct,
            COUNT(*) AS total 
        FROM user_answers ua
        WHERE ua.result_id = :resultId
    """)
    suspend fun getResultStats(resultId: Long): ResultStats

    data class ResultStats(
        @ColumnInfo(name = "correct") val correctAnswers: Int,
        @ColumnInfo(name = "total") val totalAnswers: Int
    )
    // endregion

    // region Test Session Management
    @Transaction
    suspend fun startTestSession(testId: Long): Long {
        val result = TestResultEntity(
            testId = testId,
            status = "in_progress",
            currentQuestionOrder = 1,
            remainingSeconds = null,
            finishedAt = null,
            correctPercentage = 0.0
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

    @Query("SELECT * FROM user_answers WHERE result_id = :resultId AND question_id = :questionId")
    suspend fun getUserAnswer(resultId: Long, questionId: Long): UserAnswerEntity?
    // endregion

    // region Metadata
    @Query("SELECT duration_minutes FROM tests WHERE test_id = :testId")
    suspend fun getTestDuration(testId: Long): Int

    @Query("SELECT COUNT(*) FROM questions WHERE test_id = :testId")
    suspend fun getQuestionCount(testId: Long): Int

    @Query("SELECT remaining_seconds FROM test_results WHERE result_id = :resultId")
    suspend fun getRemainingSeconds(resultId: Long): Int?

    @Query("UPDATE test_results SET remaining_seconds = :seconds WHERE result_id = :resultId")
    suspend fun updateRemainingSeconds(resultId: Long, seconds: Int)

    @Query("""
        SELECT * FROM test_results 
        WHERE status = 'in_progress'
        ORDER BY result_id DESC 
        LIMIT 1
    """)
    suspend fun getLastInProgressResult(): TestResultEntity?

    @Query("""
        SELECT * FROM tests 
        WHERE test_id = :testId
    """)
    suspend fun getTestWithStatsById(testId: Long): TestWithStats?
}