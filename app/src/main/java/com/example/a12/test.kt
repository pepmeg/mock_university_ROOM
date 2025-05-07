package com.example.a12

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    // View-ссылки
    private lateinit var titleView: TextView
    private lateinit var questionTv: TextView
    private lateinit var answersGroup: RadioGroup
    private lateinit var scrollQuestions: HorizontalScrollView
    private lateinit var dotsContainer: LinearLayout
    private lateinit var timerText: TextView
    private lateinit var backIcon: ImageView
    private lateinit var nextButtonTextView: TextView

    // Сессия
    private lateinit var dbHelper: DbHelper
    private var testId: Int = 0
    private var resultId: Long = 0L
    private var reviewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        // Смотрим, пришли ли мы в режиме ревью
        reviewMode = intent.getBooleanExtra("REVIEW_MODE", false)
        testId     = intent.getIntExtra("TEST_ID", 1)
        dbHelper   = DbHelper(this)

        if (reviewMode) {
            // В режиме ревью используем переданный resultId и не запускаем таймер
            resultId = intent.getLongExtra("RESULT_ID", -1L)
        } else {
            // Обычный режим: заводим новую запись и таймер
            resultId = dbHelper.startTestSession(testId)
        }

        initViews()
        loadTestData()

        if (!reviewMode) {
            countDownTimer = startCountdown(
                minutes   = getTestDuration(),
                timerText = timerText,
                context   = this
            ) {
                // Время вышло — завершаем сессию и закрываем
                dbHelper.finishTestSession(resultId)
                finish()
            }
        }

        setupQuestionNumberDots(questions, dotsContainer, this) { displayQuestion(it) }
        displayQuestion(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun initViews() {
        titleView          = findViewById(R.id.title)
        questionTv         = findViewById(R.id.question)
        answersGroup       = findViewById(R.id.answersContainer)
        scrollQuestions    = findViewById(R.id.scrollQuestionNumbers)
        dotsContainer      = findViewById(R.id.questionNumbersContainer)
        timerText          = findViewById(R.id.timerText)
        backIcon           = findViewById(R.id.backIcon)
        nextButtonTextView = findViewById(R.id.nextButtonText)

        backIcon.setOnClickListener { finish() }
    }

    private fun loadTestData() {
        questions    = dbHelper.getQuestions(testId)
        titleView.text = dbHelper.getTestName(testId)
    }

    private fun getTestDuration(): Int =
        dbHelper.getTestDurationMinutes(testId)

    private fun displayQuestion(index: Int) {
        // Сохраняем факт ответа предыдущего вопроса (в answered) для UI
        if (!reviewMode && index != currentIndex && checkAnswered(currentIndex)) {
            answered.add(currentIndex)
        }
        currentIndex = index

        // Обновляем кружки навигации
        updateDotsUI(dotsContainer, currentIndex, answered, this)
        scrollToCurrentDot(dotsContainer, scrollQuestions, currentIndex)

        // Настраиваем текст вопроса и варианты
        val q = questions[index]
        questionTv.text = q.text

        val answers       = dbHelper.getAnswers(q.id)
        val savedAnswerId = if (reviewMode)
            dbHelper.getUserAnswer(resultId, q.id)
        else
            dbHelper.getUserAnswer(resultId, q.id) // в обычном режиме тоже восстанавливаем, если возвращаемся назад

        renderAnswers(
            context          = this,
            answersGroup     = answersGroup,
            answers          = answers,
            questionId       = q.id,
            selectedAnswerId = savedAnswerId
        ) { questionId, answerId ->
            // При выборе в обычном режиме сохраняем
            if (!reviewMode) {
                val isCorr = if (answers.first { it.id == answerId }.isCorrect) 1 else 0
                dbHelper.saveUserAnswer(
                    resultId       = resultId,
                    questionId     = questionId,
                    answerId       = answerId,
                    freeTextAnswer = null,
                    isCorrect      = isCorr
                )
                answered.add(currentIndex)
                updateDotsUI(dotsContainer, currentIndex, answered, this)
            }
        }

        if (reviewMode) {
            // Отключаем RadioButton и подсвечиваем фон
            for (i in 0 until answersGroup.childCount) {
                val rb = answersGroup.getChildAt(i) as RadioButton
                rb.isEnabled = false
                val aid   = rb.id
                val answer = answers.first { it.id == aid }

                val bgRes = when {
                    aid == savedAnswerId && !answer.isCorrect ->
                        R.drawable.wrong_answer
                    answer.isCorrect ->
                        R.drawable.correctly_answer
                    else ->
                        // нейтральный фон
                        if (answer.text.length > 50)
                            R.drawable.bg_answer_neutral_long
                        else
                            R.drawable.bg_answer_neutral_short
                }
                rb.background = ContextCompat.getDrawable(this, bgRes)
            }
        }

        // Кнопка Next / Wrap up
        nextButtonTextView.text = if (currentIndex == questions.lastIndex)
            "Wrap up"
        else
            "Next"
    }

    private fun checkAnswered(idx: Int): Boolean =
        idx in questions.indices &&
                dbHelper.getUserAnswer(resultId, questions[idx].id) != null

    fun onPreviousClicked(view: View) {
        if (currentIndex > 0) displayQuestion(currentIndex - 1)
    }

    fun onNextClicked(view: View) {
        if (currentIndex < questions.lastIndex) {
            displayQuestion(currentIndex + 1)
        } else {
            if (!reviewMode) {
                // Завершаем тестовую сессию и запускаем Complete
                dbHelper.finishTestSession(resultId)
                Intent(this, CompleteActivity::class.java).apply {
                    putExtra("TEST_ID",     testId)
                    putExtra("TEST_NAME",   titleView.text.toString())
                    putExtra("RESULT_ID",   resultId)
                }.also { startActivity(it) }
            }
            finish()
        }
    }
}