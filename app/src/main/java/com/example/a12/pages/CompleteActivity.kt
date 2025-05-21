package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.a12.R
import com.example.a12.model.AppDatabase
import com.example.a12.model.DAO.TestDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CompleteActivity : AppCompatActivity() {

    private lateinit var dao: TestDao
    private var resultId: Long = 1L
    private var testId: Long = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.complete)

        resultId = intent.getLongExtra("RESULT_ID", 1L)
        testId   = intent.getLongExtra("TEST_ID", 1L)
        val testName = intent.getStringExtra("TEST_NAME").orEmpty()

        dao = AppDatabase.getInstance(this).testDao()
        findViewById<TextView>(R.id.title).text = testName

        lifecycleScope.launch {

            val stats = withContext(Dispatchers.IO) {
                dao.getResultStats(resultId)
            }

            val totalQuestions = withContext(Dispatchers.IO) {
                dao.getQuestionCount(testId)
            }
            val correct = stats.correctAnswers
            val percent = if (totalQuestions > 0)
                correct * 100.0 / totalQuestions
            else 0.0

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.percentText).text =
                    String.format(Locale.getDefault(), "%.0f%%", percent)

                findViewById<TextView>(R.id.correctCountText).text =
                    "$correct/$totalQuestions"
            }
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
                .addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                .also { startActivity(it) }
            finish()
        }
    }
}