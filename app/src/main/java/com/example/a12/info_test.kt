package com.example.a12

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_test)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            finish()
        }

        val instructionText = findViewById<TextView>(R.id.instructionText)

        val bulletPoints = listOf(
            "10 point awarded for a correct answer and no marks for an incorrect answer.",
            "Tap on options to select the correct answer.",
            "Tap on the bookmark icon to save interesting questions.",
            "Click submit if you are sure you want to complete all the questions."
        )

        val spannableString = SpannableStringBuilder()
        bulletPoints.forEach {
            spannableString.append("• ").append(it).append("\n\n")
        }

        instructionText.text = spannableString
    }
}