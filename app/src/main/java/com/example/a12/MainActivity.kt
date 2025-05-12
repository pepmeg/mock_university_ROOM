package com.example.a12

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.model.TestItem
import com.example.a12.ui.TestsAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var cardContainer: FrameLayout
    private lateinit var iconIv: ImageView
    private lateinit var titleTv: TextView
    private lateinit var progressTv: TextView
    private lateinit var remainingTv: TextView
    private lateinit var dbHelper: DbHelper
    private lateinit var recycler: RecyclerView
    private lateinit var navHandler: BottomNavHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper   = DbHelper(this)
        navHandler = BottomNavHandler(this, findViewById(android.R.id.content))
        navHandler.setupNavigation()

        cardContainer = findViewById(R.id.cardContainer)
        iconIv        = cardContainer.findViewById(R.id.cardIcon)
        titleTv       = cardContainer.findViewById(R.id.cardTitle)
        progressTv    = cardContainer.findViewById(R.id.cardProgress)
        remainingTv   = cardContainer.findViewById(R.id.cardRemaining)

        recycler = findViewById(R.id.testsRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = TestsAdapter(
            items       = dbHelper.getAllTestItems(),
            layoutResId = R.layout.item_test_card
        ) { test ->
            startActivity(Intent(this, InfoTestActivity::class.java).apply {
                putExtra("TEST_ID",       test.id)
                putExtra("TEST_NAME",     test.name)
                putExtra("TEST_DURATION", test.durationMinutes)
                putExtra("TEST_Q_COUNT",  test.questionsCount)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        updateContinueCard()

        // Обновляем список тестов
        val updatedTests = dbHelper.getAllTestItems()
        (recycler.adapter as? TestsAdapter)?.updateItems(updatedTests)

        // Обновляем нижнюю навигацию
        val prefs = getSharedPreferences("bottom_nav", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_button", "home").apply()
        navHandler.setupNavigation()
    }

    private fun updateContinueCard() {
        val lastRes = dbHelper.getLastResultForTestForAnyTest()
        if (lastRes != null && lastRes.second == "in_progress") {
            val (resultId, _) = lastRes
            val testId = dbHelper.getTestIdByResult(resultId)
            val item   = dbHelper.getTestItemById(testId)

            cardContainer.isVisible = true

            // иконка, текст, прогресс
            val rid = resources.getIdentifier(item.iconResName, "drawable", packageName)
            if (rid != 0) iconIv.setImageResource(rid)
            titleTv.text    = item.name
            progressTv.text = "${item.answeredCount}/${item.questionsCount}"
            remainingTv.text = "${item.remainingSeconds/60} min"

            // клик по самой карточке
            cardContainer.setOnClickListener {
                startActivity(Intent(this, TestActivity::class.java).apply {
                    putExtra("TEST_ID",    item.id)
                    putExtra("RESULT_ID",  resultId)
                    putExtra("REVIEW_MODE", false)
                })
            }
            val deleteIv = cardContainer.findViewById<ImageView>(R.id.cardDelete)
            deleteIv.setOnClickListener {
                cardContainer.isVisible = false
                dbHelper.finishTestSession(resultId)
            }
        } else {
            cardContainer.isVisible = false
        }
    }
}