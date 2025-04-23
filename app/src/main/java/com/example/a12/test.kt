package com.example.a12

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class test : AppCompatActivity(){
    private var selectedTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onQuestionNumberClicked(view: View) {
        if (view is TextView) {
            selectTextView(view)
        }
    }

    private fun selectTextView(textView: TextView) {
        selectedTextView?.let {
            it.background = getDrawable(R.drawable.bg_circle_unselected)
        }

        textView.background = getDrawable(R.drawable.bg_circle_selected)
        selectedTextView = textView
    }
}