package com.example.a12

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.a12.model.AppDatabase
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.entities.AnswerEntity
import com.example.a12.model.entities.QuestionEntity
import com.example.a12.model.entities.TestEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.a12", appContext.packageName)
    }
}

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TestDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TestDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.testDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetTest() = runBlocking {
        val test = TestEntity(testName = "Java Test", durationMinutes = 10)
        val testId = dao.insertTest(test)
        val loaded = dao.getTestById(testId)
        assertNotNull(loaded)
        assertEquals("Java Test", loaded?.testName)
    }

    @Test
    fun insertAndGetQuestions() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "Test", durationMinutes = 15)).toInt()
        dao.insertQuestion(
            QuestionEntity(
                testId = testId,
                questionText = "What is Java?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 1
            )
        )
        val questions = dao.getQuestions(testId.toLong())
        assertEquals(1, questions.size)
        assertEquals("What is Java?", questions.first().questionText)
    }

    @Test
    fun testCountShouldIncrease() = runBlocking {
        assertEquals(0, dao.getTestCount())
        dao.insertTest(TestEntity(testName = "One", durationMinutes = 5))
        assertEquals(1, dao.getTestCount())
    }

    @Test
    fun getTestNameById() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "React", durationMinutes = 10))
        val name = dao.getTestName(testId)
        assertEquals("React", name)
    }

    @Test
    fun insertAndOrderQuestions() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "C++", durationMinutes = 20))
        val q1 = QuestionEntity(
            testId = testId.toInt(),
            questionText = "Q1",
            questionType = "single",
            minAnswers = 1,
            maxAnswers = 1,
            orderNumber = 2
        )

        val q2 = QuestionEntity(
            testId = testId.toInt(),
            questionText = "Q2",
            questionType = "single",
            minAnswers = 1,
            maxAnswers = 1,
            orderNumber = 1
        )

        dao.insertQuestion(q1)
        dao.insertQuestion(q2)

        val questions = dao.getQuestions(testId)
        assertEquals("Q2", questions[0].questionText)
        assertEquals("Q1", questions[1].questionText)
    }

    @Test
    fun startAndFinishTestSession() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "React", durationMinutes = 15))
        val resultId = dao.startTestSession(testId)

        dao.finishTestSession(resultId, 60)

        val result = dao.getTestResult(resultId)
        assertEquals("completed", result?.status)
        assertEquals(60, result?.remainingSeconds)
    }
    @Test
    fun saveUserAnswerCreatesOrUpdates() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "Kotlin", durationMinutes = 10))

        val resultId = dao.startTestSession(testId)

        val question = QuestionEntity(
            testId = testId.toInt(),
            questionText = "Q?",
            questionType = "single",
            minAnswers = 1,
            maxAnswers = 1,
            orderNumber = 1
        )
        val questionId = dao.insertQuestion(question)

        val answer = AnswerEntity(
            questionId = questionId.toInt(),
            answerText = "42",
            isCorrect = true
        )
        val answerId = dao.insertAnswer(answer)

        dao.saveUserAnswer(resultId, questionId, answerId, null, true)
        val userAnswer = dao.getUserAnswer(resultId, questionId)
        assertNotNull(userAnswer)
        assertEquals(answerId, userAnswer?.answerId)

        dao.saveUserAnswer(resultId, questionId, answerId, null, false)
        val updated = dao.getUserAnswer(resultId, questionId)
        assertEquals(false, updated?.isCorrect)
    }

    @Test
    fun deleteTestResults_removesResults() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "With Results", durationMinutes = 5))
        val resultId = dao.startTestSession(testId)

        assertNotNull(dao.getTestResult(resultId))

        dao.deleteTestResults(testId)

        assertNull(dao.getTestResult(resultId))
    }


}