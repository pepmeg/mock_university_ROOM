package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.a12.model.DbHelper
import com.example.a12.R

class InfoTestActivity : AppCompatActivity() {

    private var testId: Int = 0
    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_test)

        dbHelper = DbHelper(this)
        testId = intent.getIntExtra("TEST_ID", 1)

        findViewById<TextView>(R.id.title).text =
            intent.getStringExtra("TEST_NAME") ?: ""
        findViewById<TextView>(R.id.title1).text =
            intent.getStringExtra("TEST_NAME") ?: ""
        val duration = intent.getIntExtra("TEST_DURATION", 0)
        findViewById<TextView>(R.id.testDuration)?.text =
            "${duration / 60}h ${duration % 60}min"
        val qCount = intent.getIntExtra("TEST_Q_COUNT", 0)
        findViewById<TextView>(R.id.questionsCount)?.text = "$qCount Questions"

        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            finish()
        }

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

        findViewById<FrameLayout>(R.id.startTestContainer).setOnClickListener {

            val last = dbHelper.getLastResultForTest(testId)
            val intent = Intent(this, TestActivity::class.java)
                .putExtra("TEST_ID", testId)

            if (last != null) {
                when (last.second) {
                    "in_progress" -> {
                        intent.putExtra("REVIEW_MODE", false)
                        intent.putExtra("RESULT_ID", last.first)
                    }
                    "completed" -> {
                        intent.putExtra("REVIEW_MODE", true)
                        intent.putExtra("RESULT_ID", last.first)
                    }
                }
            } else {
                intent.putExtra("REVIEW_MODE", false)
            }
            startActivity(intent)
        }
    }
}