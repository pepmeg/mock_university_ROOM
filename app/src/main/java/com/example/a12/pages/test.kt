package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.a12.R
import com.example.a12.model.AppDatabase
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.entities.QuestionEntity
import com.example.a12.model.entities.UserAnswerEntity
import com.example.a12.utils.countdown.startCountdown
import com.example.a12.utils.dots.*
import com.example.a12.utils.answers.renderAnswers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestActivity : AppCompatActivity() {

    private lateinit var testDao: TestDao

    private var questions: List<QuestionEntity> = emptyList()

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
    private lateinit var progressBar: ProgressBar

    private var testId: Long = 1
    private var resultId: Long = -1
    private var reviewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        testDao = AppDatabase.getInstance(this).testDao()

        reviewMode = intent.getBooleanExtra("REVIEW_MODE", false)
        testId = intent.getLongExtra("TEST_ID", 1L)
        resultId = intent.getLongExtra("RESULT_ID", -1)
        timesUpOverlay = findViewById(R.id.timesUpOverlay)
        submitButton = timesUpOverlay.findViewById(R.id.submitButton)

        initViews()

        lifecycleScope.launch {
            questions = withContext(Dispatchers.IO) {
                testDao.getQuestions(testId)
            }
            if (questions.isEmpty()) {
                finish()
                return@launch
            }
            if (!reviewMode && resultId < 0) {
                resultId = withContext(Dispatchers.IO) {
                    testDao.startTestSession(testId)
                }
            }

            titleView.text = withContext(Dispatchers.IO) {
                testDao.getTestName(testId)
            }

            if (!reviewMode) {
                val initialMillis = withContext(Dispatchers.IO) {
                    testDao.getRemainingSeconds(resultId)?.takeIf { it > 0 }?.let { it * 1000L }
                        ?: run {
                            val testIdFromRes = testDao.getTestIdByResult(resultId)
                            val minutes       = testDao.getTestDuration(testIdFromRes)
                            minutes * 60_000L
                        }
                }
                countDownTimer = startCountdown(
                    initialMillis = initialMillis,
                    timerText     = timerText,
                    resultId      = resultId,
                    testDao       = testDao,
                    scope         = lifecycleScope,
                    onFinish      = { onTimeUp() }
                )
            }

            progressBar.max = questions.size
            setupQuestionNumberDots(questions, dotsContainer, this@TestActivity) { index ->
                lifecycleScope.launch {
                    displayQuestion(index)
                }
            }
            displayQuestion(0)
        }

        submitButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    testDao.finishTestSession(resultId, (millisUntilFinished / 1000).toInt())
                }
                navigateToComplete()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun initViews() {
        titleView = findViewById(R.id.title)
        questionTv = findViewById(R.id.question)
        answersGroup = findViewById(R.id.answersContainer)
        scrollQuestions = findViewById(R.id.scrollQuestionNumbers)
        dotsContainer = findViewById(R.id.questionNumbersContainer)
        timerText = findViewById(R.id.timerText)
        backIcon = findViewById(R.id.backIcon)
        timerContainer = findViewById(R.id.timerContainer)
        nextButtonTextView = findViewById(R.id.nextButtonText)
        explanationContainer = findViewById(R.id.explanationContainer)
        explanationText = findViewById(R.id.explanationText)
        backIcon.setOnClickListener { onBackPressed() }
        timerContainer.isVisible = !reviewMode
        progressBar = findViewById(R.id.testProgressBar)
    }

    private suspend fun displayQuestion(index: Int) {
        if (!reviewMode  && checkAnswered(currentIndex)) {
            answered.add(currentIndex)
        }
        currentIndex = index
        progressBar.progress = index + 1
        withContext(Dispatchers.Main) {
            updateDotsUI(
                container = dotsContainer,
                currentIndex = currentIndex,
                context = this@TestActivity,
                testDao = testDao,
                questions = questions,
                resultId = resultId,
                reviewMode = reviewMode
            )
            scrollToCurrentDot(dotsContainer, scrollQuestions, currentIndex)
        }

        val q = questions[index]
        questionTv.text = q.questionText

        val answerEntities = withContext(Dispatchers.IO) {
            testDao.getAnswers(q.questionId.toLong())
        }

        val savedAnswerEntity: UserAnswerEntity? = withContext(Dispatchers.IO) {
            testDao.getUserAnswer(resultId, q.questionId.toLong())
        }

        val savedAnswerId: Long? = savedAnswerEntity?.answerId

        withContext(Dispatchers.Main) {
            renderAnswers(
                context          = this@TestActivity,
                answersGroup     = answersGroup,
                answers          = answerEntities,
                questionId       = q.questionId,
                selectedAnswerId = savedAnswerId?.toInt()
            ) { questionId, answerId ->
                val sel = answerEntities.first { it.answerId == answerId }
                lifecycleScope.launch(Dispatchers.IO) {
                    testDao.saveUserAnswer(
                        resultId   = resultId,
                        questionId = questionId.toLong(),
                        answerId   = answerId.toLong(),
                        freeText   = null,
                        isCorrect  = sel.isCorrect
                    )
                    testDao.updateRemainingTime(resultId, (millisUntilFinished / 1000).toInt())
                }
            }

            if (reviewMode) {
                for (i in 0 until answersGroup.childCount) {
                    val rb  = answersGroup.getChildAt(i) as RadioButton
                    val aid = rb.id
                    val ent = answerEntities.first { it.answerId == aid }

                    val bgRes = when {
                        savedAnswerId?.toInt() == aid && !ent.isCorrect -> R.drawable.wrong_answer
                        ent.isCorrect                                   -> R.drawable.correctly_answer
                        ent.answerText.length > 50                      -> R.drawable.bg_answer_neutral_long
                        else                                            -> R.drawable.bg_answer_neutral_short
                    }

                    rb.isEnabled = false
                    rb.background = ContextCompat.getDrawable(this@TestActivity, bgRes)
                }
            }
        }
    }

    private suspend fun checkAnswered(idx: Int): Boolean {
        if (idx !in questions.indices) return false
        return withContext(Dispatchers.IO) {
            testDao.getUserAnswer(resultId, questions[idx].questionId.toLong()) != null
        }
    }

    fun onPreviousClicked(view: View) {
        if (currentIndex > 0) {
            lifecycleScope.launch {
                displayQuestion(currentIndex - 1)
            }
        }
    }

    fun onNextClicked(view: View) {
        lifecycleScope.launch {
            if (currentIndex < questions.lastIndex) {
                displayQuestion(currentIndex + 1)
            } else {
                if (!reviewMode) {
                    withContext(Dispatchers.IO) {
                        testDao.finishTestSession(resultId, (millisUntilFinished / 1000).toInt())
                    }
                    navigateToComplete()
                }
                finish()
            }
        }
    }

    private fun onTimeUp() {
        timesUpOverlay.visibility = View.VISIBLE
        findViewById<View>(R.id.rootFrame).isClickable = false
    }

    private fun navigateToComplete() {
        Intent(this, CompleteActivity::class.java).apply {
            putExtra("TEST_ID", testId)
            putExtra("TEST_NAME", titleView.text.toString())
            putExtra("RESULT_ID", resultId)
        }.also { startActivity(it) }
    }
}