package com.example.a12

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var navHandler: BottomNavHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHandler = BottomNavHandler(this, findViewById(android.R.id.content))
        navHandler.setupNavigation()

        // Открытие теста по клику на карточку AWS
        val awsCard = findViewById<FrameLayout>(R.id.awsCardLayout)
        awsCard.setOnClickListener {
            startActivity(Intent(this, InfoTestActivity::class.java))
        }

        // Делаем фон прозрачным у контейнера карточки
        val cardContainer = findViewById<FrameLayout>(R.id.cardContainer)
        makeViewTransparent(cardContainer)  // убирает любой фон, заданный в XML или стилях

    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("bottom_nav", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_button", "home").apply()
        navHandler.setupNavigation()
    }

    /**
     * Удаляет фон у переданного View:
     * 1) setBackgroundColor(Color.TRANSPARENT) — задаёт полностью прозрачный цвет фона.
     * 2) background = null             — удаляет drawable-фон совсем.
     */
    private fun makeViewTransparent(view: View) {
        view.setBackgroundColor(Color.TRANSPARENT)  // полностью прозрачный фон :contentReference[oaicite:0]{index=0}
        view.background = null                      // удаляет background-дравебл :contentReference[oaicite:1]{index=1}
    }
}