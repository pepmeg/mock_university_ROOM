package com.example.a12

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.a12.model.Question
import com.example.a12.model.Answer

class TestActivity : AppCompatActivity() {

    private lateinit var questions: List<Question>
    private var currentIndex = 0
    private val answered = mutableSetOf<Int>()
    private var countDownTimer: CountDownTimer? = null

    // View-ссылки
    private lateinit var questionTv: TextView
    private lateinit var answersContainer: LinearLayout
    private lateinit var scrollQuestions: HorizontalScrollView
    private lateinit var dotsContainer: LinearLayout
    private lateinit var timerText: TextView
    private lateinit var backIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        // 1. Инициализируем View
        questionTv       = findViewById(R.id.question)
        answersContainer = findViewById(R.id.answersContainer)
        scrollQuestions  = findViewById(R.id.scrollQuestionNumbers)
        dotsContainer    = findViewById(R.id.questionNumbersContainer)
        timerText        = findViewById(R.id.timerText)         // TextView в timerContainer
        backIcon         = findViewById(R.id.backIcon)

        backIcon.setOnClickListener { finish() }
        // 1) Инициализируем DbHelper и получаем testId
        val dbHelper = DbHelper(this)
        val testId = intent.getIntExtra("TEST_ID", 1)

        // 2) Получаем название теста из БД и сразу устанавливаем в TextView
        val titleView: TextView = findViewById(R.id.title)
        val testName = dbHelper.getTestName(testId)
        titleView.text = testName                          // установка текста :contentReference[oaicite:1]{index=1}
        // 2. Загружаем данные из БД
        val db = DbHelper(this)
        questions = db.getQuestions(testId)
        val durationMinutes = db.getTestDurationMinutes(testId)

        // 3. Стартуем таймер обратного отсчёта
        startCountdown(durationMinutes)

        // 4. Рисуем кружки-номера вопросов
        setupQuestionNumberDots()

        // 5. Показываем первый вопрос
        displayQuestion(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    //--------------------------------------------------------------------------------------------
    //                          ТАЙМЕР ОБРАТНОГО ОТСЧЁТА
    //--------------------------------------------------------------------------------------------
    private fun startCountdown(minutes: Int) {
        val totalMillis = minutes * 60_000L
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalMillis, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val m = millisUntilFinished / 60_000
                val s = (millisUntilFinished % 60_000) / 1_000
                timerText.text = String.format("%d:%02d", m, s)
            }
            override fun onFinish() {
                timerText.text = "0:00"
                // TODO: завершить тест автоматически
            }
        }.start()
    }

    //--------------------------------------------------------------------------------------------
    //                  ДИНАМИЧЕСКИЕ КРУЖКИ-НОМЕРА ВОПРОСОВ
    //--------------------------------------------------------------------------------------------
    private fun setupQuestionNumberDots() {
        dotsContainer.removeAllViews()
        val density = resources.displayMetrics.density
        val sizePx   = (40 * density).toInt()  // 40 dp
        val marginPx = (8  * density).toInt()  // 8 dp

        questions.forEachIndexed { index, _ ->
            val dot = TextView(this).apply {
                text       = (index + 1).toString()
                gravity    = Gravity.CENTER
                textSize   = 14f
                setOnClickListener { displayQuestion(index) }
                // начальное оформление
                background   = ContextCompat.getDrawable(context, R.drawable.bg_circle_unselected)
                setTextColor(ContextCompat.getColor(context, R.color.text_unselected))
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    marginEnd = marginPx
                }
            }
            dotsContainer.addView(dot)
        }
    }

    private fun updateDots() {
        for (i in 0 until dotsContainer.childCount) {
            val tv = dotsContainer.getChildAt(i) as TextView
            when {
                i == currentIndex -> {
                    tv.background = ContextCompat.getDrawable(this, R.drawable.bg_circle_black)
                    tv.setTextColor(Color.WHITE)
                }
                answered.contains(i) -> {
                    tv.background = ContextCompat.getDrawable(this, R.drawable.bg_circle_selected)
                    tv.setTextColor(Color.WHITE)
                }
                else -> {
                    tv.background = ContextCompat.getDrawable(this, R.drawable.bg_circle_unselected)
                    tv.setTextColor(ContextCompat.getColor(this, R.color.text_unselected))
                }
            }
        }
    }

    private fun scrollToCurrentDot() {
        val tv = dotsContainer.getChildAt(currentIndex)
        scrollQuestions.post {
            val scrollX = tv.left - (scrollQuestions.width - tv.width) / 2
            scrollQuestions.smoothScrollTo(scrollX, 0)
        }
    }

    //--------------------------------------------------------------------------------------------
    //                             ОТОБРАЖЕНИЕ ВОПРОСОВ И ОТВЕТОВ
    //--------------------------------------------------------------------------------------------
    private fun displayQuestion(index: Int) {
        // помечаем предыдущий отвеченным
        if (index != currentIndex && checkAnswered(currentIndex)) {
            answered.add(currentIndex)
        }
        currentIndex = index

        // обновляем кружки
        updateDots()
        scrollToCurrentDot()

        // текст вопроса
        val q = questions[index]
        questionTv.text = q.text

        // варианты ответов
        val answers = DbHelper(this).getAnswers(q.id)
        for (i in 0 until answersContainer.childCount) {
            val child = answersContainer.getChildAt(i)
            if (child is ToggleButton && i < answers.size) {
                child.textOff   = answers[i].text
                child.textOn    = answers[i].text
                child.isChecked = false
            }
        }
    }

    private fun checkAnswered(idx: Int): Boolean {
        // TODO: здесь ваша логика — смотрим в user_answers, был ли ответ
        return true
    }

    //--------------------------------------------------------------------------------------------
    //                             КНОПКИ PREVIOUS/NEXT
    //--------------------------------------------------------------------------------------------
    fun onPreviousClicked(view: View) {
        if (currentIndex > 0) displayQuestion(currentIndex - 1)
    }

    fun onNextClicked(view: View) {
        if (currentIndex < questions.size - 1) displayQuestion(currentIndex + 1)
    }
}