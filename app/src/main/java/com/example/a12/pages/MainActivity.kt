package com.example.a12.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.utils.BottomNavHandler
import com.example.a12.model.DbHelper
import com.example.a12.R
import com.example.a12.ui.TestsAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var cardContainer: FrameLayout
    private lateinit var iconIv: ImageView
    private lateinit var titleTv: TextView
    private lateinit var progressTv: TextView
    private lateinit var remainingTv: TextView
    private lateinit var dbHelper: DbHelper
    private lateinit var recycler: RecyclerView
    private lateinit var recycler1: RecyclerView
    private lateinit var recycler2: RecyclerView

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
        recycler1 = findViewById(R.id.testsRecyclerView1)
        recycler2 = findViewById(R.id.testsRecyclerView2)
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
        recycler1.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler1.adapter = TestsAdapter(
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
        recycler2.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler2.adapter = TestsAdapter(
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

        val updatedTests = dbHelper.getAllTestItems()
        (recycler.adapter as? TestsAdapter)?.updateItems(updatedTests)
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

            val rid = resources.getIdentifier(item.iconResName, "drawable", packageName)
            if (rid != 0) iconIv.setImageResource(rid)
            titleTv.text    = item.name
            progressTv.text = "${item.answeredCount}/${item.questionsCount}"
            remainingTv.text = "${item.remainingSeconds/60} min"

            val progressBar = cardContainer.findViewById<ProgressBar>(R.id.cardProgressBar)
            val percent = if (item.questionsCount > 0) {
                (item.answeredCount * 100 / item.questionsCount)
            } else 0

            progressBar.progress = percent
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
            }
        } else {
            cardContainer.isVisible = false
        }
    }
}