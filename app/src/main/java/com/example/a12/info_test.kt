package com.example.a12

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoTestActivity : AppCompatActivity() {

    private var testId: Int = 0
    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_test)

        // 0) Инициализируем helper
        dbHelper = DbHelper(this)

        // 1) Читаем ID теста
        testId = intent.getIntExtra("TEST_ID", 1)

        // 2) Заполняем UI
        findViewById<TextView>(R.id.title).text =
            intent.getStringExtra("TEST_NAME") ?: ""
        findViewById<TextView>(R.id.title1).text =
            intent.getStringExtra("TEST_NAME") ?: ""
        val duration = intent.getIntExtra("TEST_DURATION", 0)
        findViewById<TextView>(R.id.testDuration)?.text =
            "${duration / 60}h ${duration % 60}min"
        val qCount = intent.getIntExtra("TEST_Q_COUNT", 0)
        findViewById<TextView>(R.id.questionsCount)?.text = "$qCount Questions"

        // 3) Назад
        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            finish()
        }

        // 4) Инструкции
        val bullets = listOf(
            "10 point awarded for a correct answer and no marks for an incorrect answer.",
            "Tap on options to select the correct answer.",
            "Tap on the bookmark icon to save interesting questions.",
            "Click submit if you are sure you want to complete all the questions."
        )
        val instr = findViewById<TextView>(R.id.instructionText)
        SpannableStringBuilder().apply {
            bullets.forEach {
                append("• ").append(it).append("\n\n")
            }
            instr.text = this
        }

        // 5) Кнопка «Start Test»
        findViewById<FrameLayout>(R.id.startTestContainer).setOnClickListener {
            // Берём последнюю сессию по этому тесту
            val last = dbHelper.getLastResultForTest(testId)

            // Всегда передаём TEST_ID
            val intent = Intent(this, TestActivity::class.java)
                .putExtra("TEST_ID", testId)

            if (last != null) {
                when (last.second) {
                    "in_progress" -> {
                        // незавершённый тест — просто продолжаем
                        intent.putExtra("REVIEW_MODE", false)
                        intent.putExtra("RESULT_ID", last.first)
                    }
                    "completed" -> {
                        // завершён — сразу в режим обзора
                        intent.putExtra("REVIEW_MODE", true)
                        intent.putExtra("RESULT_ID", last.first)
                    }
                }
            } else {
                // ни одной сессии не было — новый тест
                intent.putExtra("REVIEW_MODE", false)
            }

            startActivity(intent)
        }
    }
}