package com.example.a12

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.a12.model.Question
import com.example.a12.utils.countdown.startCountdown
import com.example.a12.utils.dots.*
import com.example.a12.utils.answers.renderAnswers

class TestActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private lateinit var questions: List<Question>
    private var currentIndex = 0
    private val answered = mutableSetOf<Int>()
    private var countDownTimer: CountDownTimer? = null
    private var millisUntilFinished: Long = 0L

    private lateinit var titleView: TextView
    private lateinit var questionTv: TextView
    private lateinit var answersGroup: RadioGroup
    private lateinit var scrollQuestions: HorizontalScrollView
    private lateinit var dotsContainer: LinearLayout
    private lateinit var timerText: TextView
    private lateinit var backIcon: ImageView
    private lateinit var nextButtonTextView: TextView
    private lateinit var timerContainer: LinearLayout
    private lateinit var explanationContainer: LinearLayout
    private lateinit var explanationText: TextView
    private lateinit var timesUpOverlay: View
    private lateinit var submitButton: View

    // Параметры сессии
    private var testId: Int = -1
    private var resultId: Long = -1L
    private var reviewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        dbHelper = DbHelper(this)

        // 1) Читаем входные параметры
        reviewMode = intent.getBooleanExtra("REVIEW_MODE", false)
        testId     = intent.getIntExtra("TEST_ID", -1)
        resultId   = intent.getLongExtra("RESULT_ID", -1L)
        timesUpOverlay = findViewById(R.id.timesUpOverlay)
        submitButton   = timesUpOverlay.findViewById(R.id.submitButton)

        // 2) Загружаем вопросы
        questions = dbHelper.getQuestions(testId)
        if (questions.isEmpty()) {
            finish(); return
        }
        // 3) Если это новая сессия (не review и resultId не передан) — стартуем её
        if (!reviewMode && resultId < 0) {
            resultId = dbHelper.startTestSession(testId)
        }

        submitButton.setOnClickListener {
            dbHelper.finishTestSession(resultId, (millisUntilFinished / 1000).toInt())
            navigateToComplete()
            finish()
        }

        initViews()
        titleView.text = dbHelper.getTestName(testId)

        if (!reviewMode) {
            val initialMillis = dbHelper.getInitialMillis(resultId)
            countDownTimer = startCountdown(
                initialMillis = initialMillis,
                timerText     = timerText,
                resultId      = resultId,
                dbHelper      = dbHelper
            ) {
                onTimeUp()
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
        timerContainer     = findViewById(R.id.timerContainer)
        nextButtonTextView = findViewById(R.id.nextButtonText)
        explanationContainer = findViewById(R.id.explanationContainer)
        explanationText      = findViewById(R.id.explanationText)

        backIcon.setOnClickListener { onBackPressed() }
        timerContainer.isVisible = !reviewMode
    }

    private fun displayQuestion(index: Int) {
        // Сохраняем переход от предыдущего
        if (!reviewMode && index != currentIndex && checkAnswered(currentIndex)) {
            answered.add(currentIndex)
        }
        currentIndex = index

        // Обновляем точки
        updateDotsUI(
            container    = dotsContainer,
            currentIndex = currentIndex,
            answeredSet  = answered,
            context      = this,
            dbHelper     = dbHelper,
            questions    = questions,
            resultId     = resultId,
            reviewMode   = reviewMode
        )
        scrollToCurrentDot(dotsContainer, scrollQuestions, currentIndex)

        val q = questions[index]
        questionTv.text = q.text

        // --- ВОССТАНОВЛЕНИЕ ОТВЕТОВ ---
        val answers       = dbHelper.getAnswers(q.id)
        val savedAnswerId = dbHelper.getUserAnswer(resultId, q.id)

        renderAnswers(
            context          = this,
            answersGroup     = answersGroup,
            answers          = answers,
            questionId       = q.id,
            selectedAnswerId = savedAnswerId  // передаём сохранённый ID
        ) { questionId, answerId ->
            // При выборе сохраняем и обновляем
            val isCorr = if (answers.first { it.id == answerId }.isCorrect) 1 else 0
            dbHelper.saveUserAnswer(
                resultId   = resultId,
                questionId = questionId,
                answerId   = answerId,
                isCorrect  = isCorr
            )
            dbHelper.updateRemainingTime(resultId, (millisUntilFinished / 1000).toInt())
            answered.add(currentIndex)
            updateDotsUI(
                container    = dotsContainer,
                currentIndex = currentIndex,
                answeredSet  = answered,
                context      = this,
                dbHelper     = dbHelper,
                questions    = questions,
                resultId     = resultId,
                reviewMode   = reviewMode
            )
        }

        // В режиме обзора подсвечиваем правильность
        if (reviewMode) {
            for (i in 0 until answersGroup.childCount) {
                val rb     = answersGroup.getChildAt(i) as RadioButton
                val aid    = rb.id
                val answer = answers.first { it.id == aid }
                val bgRes  = when {
                    aid == savedAnswerId && !answer.isCorrect -> R.drawable.wrong_answer
                    answer.isCorrect                          -> R.drawable.correctly_answer
                    answer.text.length > 50                   -> R.drawable.bg_answer_neutral_long
                    else                                      -> R.drawable.bg_answer_neutral_short
                }
                rb.isEnabled  = false
                rb.background = ContextCompat.getDrawable(this, bgRes)
            }
            explanationContainer.isVisible = true

            explanationText.text = """
        There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable. If you are going to use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden in the middle of text. All the Lorem Ipsum generators on the Internet.
    """.trimIndent()
        } else {
            // Скрыть в обычном режиме
            explanationContainer.isVisible = false
        }

        nextButtonTextView.text = if (currentIndex == questions.lastIndex) "Wrap up" else "Next"
    }

    private fun checkAnswered(idx: Int) =
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
                dbHelper.finishTestSession(resultId, (millisUntilFinished / 1000).toInt())
                navigateToComplete()
            }
            finish()
        }
    }

    private fun onTimeUp() {
        timesUpOverlay.visibility = View.VISIBLE
        findViewById<View>(R.id.rootFrame).isClickable = false
    }

    private fun navigateToComplete() {
        Intent(this, CompleteActivity::class.java).apply {
            putExtra("TEST_ID",   testId)
            putExtra("TEST_NAME", titleView.text.toString())
            putExtra("RESULT_ID", resultId)
        }.also { startActivity(it) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}