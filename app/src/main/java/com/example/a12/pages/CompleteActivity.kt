package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.a12.model.DbHelper
import com.example.a12.R
import java.util.Locale

class CompleteActivity : AppCompatActivity() {

    private var resultId: Long = -1L
    private var testId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.complete)

        resultId = intent.getLongExtra("RESULT_ID", -1L)
        testId   = intent.getIntExtra("TEST_ID", -1)
        val testName = intent.getStringExtra("TEST_NAME").orEmpty()

        val db = DbHelper(this)

        findViewById<TextView>(R.id.title).text = testName

        val correct = db.getCorrectAndTotalCounts(resultId).first
        val total   = db.getTotalQuestionCount(testId)
        val percent = if (total > 0) correct * 100.0 / total else 0.0

        findViewById<TextView>(R.id.percentText).apply {
            text = String.format(Locale.getDefault(), "%.0f%%", percent)
        }

        val totalQuestions = db.getTotalQuestionCount(testId)
        findViewById<TextView>(R.id.correctCountText).apply {
            text = "$correct/$totalQuestions"
        }

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

        findViewById<FrameLayout>(R.id.wrapUpTestContainer).setOnClickListener {
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .also { startActivity(it) }
            finish()
        }
    }
}