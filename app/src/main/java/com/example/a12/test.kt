package com.example.a12

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TestActivity : AppCompatActivity() {
    private var selectedTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)
    }

    fun onQuestionNumberClicked(view: View) {
        if (view is TextView) {
            selectTextView(view)
        }
    }

    private fun selectTextView(textView: TextView) {
        selectedTextView?.let {
            it.background = ContextCompat.getDrawable(this, R.drawable.bg_circle_unselected)
            it.setTextColor(ContextCompat.getColor(this, R.color.text_unselected))
        }

        textView.background = ContextCompat.getDrawable(this, R.drawable.bg_circle_selected)
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_selected))

        selectedTextView = textView
    }
}