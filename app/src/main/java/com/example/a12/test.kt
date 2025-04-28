package com.example.a12

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.a12.model.Question
import com.example.a12.utils.countdown.startCountdown
import com.example.a12.utils.dots.setupQuestionNumberDots
import com.example.a12.utils.dots.updateDotsUI
import com.example.a12.utils.dots.scrollToCurrentDot
import com.example.a12.utils.answers.renderAnswers

class TestActivity : AppCompatActivity() {

    private lateinit var questions: List<Question>
    private var currentIndex = 0
    private val answered = mutableSetOf<Int>()
    private var countDownTimer: CountDownTimer? = null

    private lateinit var titleView: TextView
    private lateinit var questionTv: TextView
    private lateinit var answersGroup: RadioGroup
    private lateinit var scrollQuestions: HorizontalScrollView
    private lateinit var dotsContainer: LinearLayout
    private lateinit var timerText: TextView
    private lateinit var backIcon: ImageView

    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        dbHelper = DbHelper(this)

        initViews()
        loadTestData()

        countDownTimer = startCountdown(getTestDuration(), timerText, this) {
            // TODO: завершить тест
        }

        setupQuestionNumberDots(questions, dotsContainer, this) { displayQuestion(it) }
        displayQuestion(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun initViews() {
        titleView       = findViewById(R.id.title)
        questionTv      = findViewById(R.id.question)
        answersGroup    = findViewById(R.id.answersContainer)
        scrollQuestions = findViewById(R.id.scrollQuestionNumbers)
        dotsContainer   = findViewById(R.id.questionNumbersContainer)
        timerText       = findViewById(R.id.timerText)
        backIcon        = findViewById(R.id.backIcon)
        backIcon.setOnClickListener { finish() }
    }

    private fun loadTestData() {
        val testId = intent.getIntExtra("TEST_ID", 1)
        questions = dbHelper.getQuestions(testId)
        titleView.text = dbHelper.getTestName(testId)
    }

    private fun getTestDuration(): Int {
        val testId = intent.getIntExtra("TEST_ID", 1)
        return dbHelper.getTestDurationMinutes(testId)
    }

    private fun displayQuestion(index: Int) {
        // сохранить факт ответа, если был
        if (index != currentIndex && checkAnswered(currentIndex)) {
            answered.add(currentIndex)
        }
        currentIndex = index

        updateDotsUI(dotsContainer, currentIndex, answered, this)
        scrollToCurrentDot(dotsContainer, scrollQuestions, currentIndex)

        val q = questions[index]
        questionTv.text = q.text

        val answers = dbHelper.getAnswers(q.id)
        val savedAnswerId = dbHelper.getUserAnswer(q.id)

        renderAnswers(
            context = this,
            answersGroup = answersGroup,
            answers = answers,
            questionId = q.id,
            selectedAnswerId = savedAnswerId
        ) { qId, aId ->
            saveUserAnswer(qId, aId)
        }

        if (savedAnswerId != null) {
            answered.add(currentIndex)
            updateDotsUI(dotsContainer, currentIndex, answered, this)
        }
    }

    private fun checkAnswered(idx: Int): Boolean {
        if (idx < 0 || idx >= questions.size) return false
        return dbHelper.getUserAnswer(questions[idx].id) != null
    }

    private fun saveUserAnswer(questionId: Int, answerId: Int) {
        dbHelper.saveUserAnswer(questionId, answerId)
        answered.add(currentIndex)
        updateDotsUI(dotsContainer, currentIndex, answered, this)
    }

    fun onPreviousClicked(view: View) {
        if (currentIndex > 0) displayQuestion(currentIndex - 1)
    }

    fun onNextClicked(view: View) {
        if (currentIndex < questions.size - 1) displayQuestion(currentIndex + 1)
    }
}