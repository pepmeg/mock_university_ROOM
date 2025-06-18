package com.example.a12

import androidx.room.Room
import com.example.a12.model.AppDatabase
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
/*
@RunWith(RobolectricTestRunner::class)
class TestDaoUnitTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TestDao

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
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
    fun insertAndRetrieveTest() = runBlocking {
        val test = TestEntity(testName = "Math Test", durationMinutes = 20)
        val id = dao.insertTest(test)
        val loaded = dao.getTestById(id)
        assertNotNull(loaded)
        assertEquals("Math Test", loaded?.testName)
    }

    @Test
    fun testCountIncreases() = runBlocking {
        assertEquals(0, dao.getTestCount())
        dao.insertTest(TestEntity(testName = "History", durationMinutes = 30))
        assertEquals(1, dao.getTestCount())
    }

    @Test
    fun insertAndRetrieveQuestions() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "Physics", durationMinutes = 15)).toInt()
        dao.insertQuestion(
            QuestionEntity(
                testId = testId,
                questionText = "What is Newton's law?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 1
            )
        )
        val questions = dao.getQuestions(testId.toLong())
        assertEquals(1, questions.size)
        assertEquals("What is Newton's law?", questions.first().questionText)
    }

    @Test
    fun orderOfQuestionsIsCorrect() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "Ordering", durationMinutes = 5)).toInt()

        val q1 = QuestionEntity(
            testId = testId,
            questionText = "Second",
            questionType = "single",
            minAnswers = 1,
            maxAnswers = 1,
            orderNumber = 2
        )
        val q2 = QuestionEntity(
            testId = testId,
            questionText = "First",
            questionType = "single",
            minAnswers = 1,
            maxAnswers = 1,
            orderNumber = 1
        )

        dao.insertQuestion(q1)
        dao.insertQuestion(q2)

        val ordered = dao.getQuestions(testId.toLong())
        assertEquals("First", ordered[0].questionText)
        assertEquals("Second", ordered[1].questionText)
    }

    @Test
    fun getTestNameByIdReturnsCorrectName() = runBlocking {
        val id = dao.insertTest(TestEntity(testName = "Biology", durationMinutes = 12))
        val name = dao.getTestName(id)
        assertEquals("Biology", name)
    }

    @Test
    fun saveUserAnswerCreatesOrUpdates() = runBlocking {
        val testId = dao.insertTest(TestEntity(testName = "Kotlin", durationMinutes = 10)).toInt()
        val resultId = dao.startTestSession(testId.toLong())

        val question = QuestionEntity(
            testId = testId,
            questionText = "Q?",
            questionType = "single",
            minAnswers = 1,
            maxAnswers = 1,
            orderNumber = 1
        )
        val questionId = dao.insertQuestion(question).toInt()

        val answer = AnswerEntity(
            questionId = questionId,
            answerText = "42",
            isCorrect = true
        )
        val answerId = dao.insertAnswer(answer).toInt()

        dao.saveUserAnswer(resultId, questionId.toLong(), answerId.toLong(), null, true)
        val userAnswer = dao.getUserAnswer(resultId, questionId.toLong())
        assertNotNull(userAnswer)
        assertEquals(answerId.toLong(), userAnswer?.answerId)

        dao.saveUserAnswer(resultId, questionId.toLong(), answerId.toLong(), null, false)
        val updated = dao.getUserAnswer(resultId, questionId.toLong())
        assertEquals(false, updated?.isCorrect)
    }
}*/