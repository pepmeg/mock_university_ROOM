package com.example.a12.pages

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
import com.example.a12.model.TestItem
import com.example.a12.ui.TestsAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var db: DbHelper
    private lateinit var nav: BottomNavHandler
    private lateinit var card: FrameLayout

    private val infoClick: (TestItem) -> Unit = { test ->
        startActivity(Intent(this, InfoTestActivity::class.java).apply {
            putExtra("TEST_ID", test.id)
            putExtra("TEST_NAME", test.name)
            putExtra("TEST_DURATION", test.durationMinutes)
            putExtra("TEST_Q_COUNT", test.questionsCount)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db  = DbHelper(this)
        nav = BottomNavHandler(this, findViewById(android.R.id.content)).also { it.setupNavigation() }

        card = findViewById(R.id.cardContainer)

        listOf(
            R.id.testsRecyclerView,
            R.id.testsRecyclerView1,
            R.id.testsRecyclerView2
        ).forEach { id ->
            findViewById<RecyclerView>(id).apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = TestsAdapter(
                    items          = db.getAllTestItems(),
                    detailed = false,
                    onClick        = infoClick,
                    onDelete       = {}
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nav.setupNavigation()
        updateContinueCard()
        (findViewById<RecyclerView>(R.id.testsRecyclerView).adapter as? TestsAdapter)
    }

    private fun updateContinueCard() {
        val last = db.getLastResultForTestForAnyTest()
        if (last?.second == "in_progress") {
            val (resId, _) = last
            val item = db.getTestItemById(db.getTestIdByResult(resId))

            card.isVisible = true
            card.findViewById<ImageView>(R.id.cardIcon)
                .setImageResource(resources.getIdentifier(item.iconResName, "drawable", packageName))
            card.findViewById<TextView>(R.id.cardTitle).text = item.name
            card.findViewById<TextView>(R.id.cardProgress).text = "${item.answeredCount}/${item.questionsCount}"
            card.findViewById<TextView>(R.id.cardRemaining).text = "${item.remainingSeconds/60} min"

            card.findViewById<ProgressBar>(R.id.cardProgressBar).progress =
                if (item.questionsCount > 0) item.answeredCount * 100 / item.questionsCount else 0

            card.setOnClickListener {
                startActivity(Intent(this, TestActivity::class.java).apply {
                    putExtra("TEST_ID", item.id)
                    putExtra("RESULT_ID", resId)
                    putExtra("REVIEW_MODE", false)
                })
            }
            card.findViewById<ImageView>(R.id.cardDelete).setOnClickListener {
                card.isVisible = false
            }
        } else {
            card.isVisible = false
        }
    }
}