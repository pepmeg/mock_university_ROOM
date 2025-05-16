package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.a12.R
import com.example.a12.model.DbHelper

class InfoTestActivity : AppCompatActivity() {

    private val db by lazy { DbHelper(this) }
    private val testId by lazy { intent.getIntExtra("TEST_ID", 0) }
    private val testName by lazy { intent.getStringExtra("TEST_NAME").orEmpty() }
    private val durationMinutes by lazy { intent.getIntExtra("TEST_DURATION", 0) }
    private val questionsCount by lazy { intent.getIntExtra("TEST_Q_COUNT", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_test)

        findViewById<TextView>(R.id.title).text = testName
        findViewById<TextView>(R.id.title1).text = testName

        findViewById<TextView>(R.id.testDuration)?.text =
            "${durationMinutes / 60}h ${durationMinutes % 60}min"
        findViewById<TextView>(R.id.questionsCount)?.text =
            "$questionsCount Questions"

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
    }

    private fun launchTest() {
        val last = db.getLastResultForTest(testId)
        Intent(this, TestActivity::class.java).also { intent ->
            intent.putExtra("TEST_ID", testId)
            when (last?.second) {
                "in_progress" -> {
                    intent.putExtra("RESULT_ID", last.first)
                    intent.putExtra("REVIEW_MODE", false)
                }
                "completed" -> {
                    intent.putExtra("RESULT_ID", last.first)
                    intent.putExtra("REVIEW_MODE", true)
                }
                else -> intent.putExtra("REVIEW_MODE", false)
            }
            startActivity(intent)
        }
    }
}