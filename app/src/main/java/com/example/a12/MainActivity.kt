package com.example.a12

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var navHandler: BottomNavHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHandler = BottomNavHandler(this, findViewById(android.R.id.content))
        navHandler.setupNavigation()

        val awsCard = findViewById<FrameLayout>(R.id.awsCardLayout)
        awsCard.setOnClickListener {
            startActivity(Intent(this, InfoTestActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("bottom_nav", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_button", "home").apply()
        navHandler.setupNavigation()
    }
}