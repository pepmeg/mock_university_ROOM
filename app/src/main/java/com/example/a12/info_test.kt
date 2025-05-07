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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_test)

        // 1. Получаем ID теста из Intent
        testId = intent.getIntExtra("TEST_ID", 1)

        // 2. Задаём всё остальное (название, время, количество вопросов)
        val name     = intent.getStringExtra("TEST_NAME") ?: ""
        val duration = intent.getIntExtra("TEST_DURATION", 0)
        val qCount   = intent.getIntExtra("TEST_Q_COUNT", 0)

        findViewById<TextView>(R.id.title).text         = name
        findViewById<TextView>(R.id.title1).text        = name
        findViewById<TextView>(R.id.testDuration)?.text = "${duration / 60}h ${duration % 60}min"
        findViewById<TextView>(R.id.questionsCount)?.text = "$qCount Questions"

        // 3. Логика для кнопки назад
        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            finish()
        }

        // 4. Текст инструкций
        val instructionText = findViewById<TextView>(R.id.instructionText)
        val bulletPoints = listOf(
            "10 point awarded for a correct answer and no marks for an incorrect answer.",
            "Tap on options to select the correct answer.",
            "Tap on the bookmark icon to save interesting questions.",
            "Click submit if you are sure you want to complete all the questions."
        )
        val spannableString = SpannableStringBuilder().apply {
            bulletPoints.forEach {
                append("• ").append(it).append("\n\n")
            }
        }
        instructionText.text = spannableString

        // 5. Навешиваем запуск теста на Start test
        findViewById<FrameLayout>(R.id.startTestContainer)
            .setOnClickListener {
                val intent = Intent(this, TestActivity::class.java).apply {
                    putExtra("TEST_ID", testId)
                }
                startActivity(intent)
            }
    }
}