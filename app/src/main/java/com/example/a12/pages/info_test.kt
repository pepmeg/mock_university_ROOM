package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.a12.R
import com.example.a12.model.AppDatabase
import kotlinx.coroutines.launch

class InfoTestActivity : AppCompatActivity() {

    private val testId by lazy { intent.getLongExtra("TEST_ID", 0L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_test)

        findViewById<ImageView>(R.id.backIcon).setOnClickListener { finish() }
        findViewById<TextView>(R.id.instructionText).apply {
            val bullets = listOf(
                "10 point awarded for a correct answer and no marks for an incorrect answer.",
                "Tap on options to select the correct answer.",
                "Tap on the bookmark icon to save interesting questions.",
                "Click submit if you are sure you want to complete all the questions."
            )
            text = SpannableStringBuilder().apply {
                bullets.forEach { append("• $it\n\n") }
            }
        }

        findViewById<FrameLayout>(R.id.startTestContainer)
            .setOnClickListener { launchTest() }

        loadTestData()
    }

    private fun loadTestData() {
        val db = AppDatabase.getInstance(this).testDao()
        lifecycleScope.launch {
            val testWithStats = db.getTestWithStatsById(testId) ?: return@launch

            findViewById<TextView>(R.id.title).text = testWithStats.test.testName
            findViewById<TextView>(R.id.title1).text = testWithStats.test.testName

            findViewById<TextView>(R.id.description).text =
                testWithStats.test.description

            val duration = testWithStats.test.durationMinutes
            findViewById<TextView>(R.id.testDuration)?.text =
                "${duration / 60}h ${duration % 60}min"

            findViewById<TextView>(R.id.questionsCount)?.text =
                "${testWithStats.totalQuestions} Questions"
        }
    }

    private fun launchTest() {
        val db = AppDatabase.getInstance(this).testDao()
        lifecycleScope.launch {
            val testWithStats = db.getTestWithStatsById(testId)
            val resultId = testWithStats?.lastResultId
            val lastStatus = testWithStats?.lastStatus

            val intent = Intent(this@InfoTestActivity, TestActivity::class.java).apply {
                putExtra("TEST_ID", testId)
                when (lastStatus) {
                    "in_progress" -> {
                        putExtra("RESULT_ID", resultId)
                        putExtra("REVIEW_MODE", false)
                    }
                    "completed" -> {
                        putExtra("RESULT_ID", resultId)
                        putExtra("REVIEW_MODE", true)
                    }
                    else -> putExtra("REVIEW_MODE", false)
                }
            }
            startActivity(intent)
        }
    }
}