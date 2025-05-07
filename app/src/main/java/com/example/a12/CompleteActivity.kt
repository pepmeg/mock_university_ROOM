package com.example.a12

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

// CompleteActivity.kt

class CompleteActivity : AppCompatActivity() {

    private var resultId: Long = -1L
    private var testId: Int     = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.complete)

        // Из Intent
        resultId = intent.getLongExtra("RESULT_ID", -1L)
        testId   = intent.getIntExtra("TEST_ID", -1)
        val testName = intent.getStringExtra("TEST_NAME").orEmpty()

        val db = DbHelper(this)

        // 1) Заголовок
        findViewById<TextView>(R.id.title).text = testName

        // 2) Процент
        val percent = db.getCorrectPercentage(resultId)
        findViewById<TextView>(R.id.percentText).apply {
            text = String.format(Locale.getDefault(), "%.0f%%", percent)
        }

        // 3) Количество правильных / всего
        val (correct, total) = db.getCorrectAndTotalCounts(resultId)
        findViewById<TextView>(R.id.correctCountText).apply {
            text = "$correct/$total "
        }

        // 4) Кнопка «назад»
        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            finish()
        }

        findViewById<FrameLayout>(R.id.reviewTestContainer).setOnClickListener {
            Intent(this, TestActivity::class.java).apply {
                putExtra("TEST_ID",   testId)
                putExtra("RESULT_ID", resultId)
                putExtra("REVIEW_MODE", true)
            }.also { startActivity(it) }
        }

        // 5) Wrap up Exam → на MainActivity
        findViewById<FrameLayout>(R.id.wrapUpTestContainer).setOnClickListener {
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .also { startActivity(it) }
            finish()
        }
    }
}