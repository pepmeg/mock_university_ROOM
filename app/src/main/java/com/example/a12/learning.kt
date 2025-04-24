package com.example.a12

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class LearningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learning)

        BottomNavHandler(this, findViewById(android.R.id.content)).setupNavigation()

        val continueTestBtn: FrameLayout = findViewById(R.id.btn_continue_test)
        continueTestBtn.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }
    }
}