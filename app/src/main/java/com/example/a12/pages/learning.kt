package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.utils.BottomNavHandler
import com.example.a12.model.DbHelper
import com.example.a12.R
import com.example.a12.ui.TestsAdapter

class LearningActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private lateinit var adapter: TestsAdapter

    private lateinit var tabAll: FrameLayout
    private lateinit var tabInProgress: FrameLayout
    private lateinit var tabCompleted: FrameLayout
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learning)

        BottomNavHandler(this, findViewById(android.R.id.content)).setupNavigation()
        dbHelper = DbHelper(this)

        tabAll = findViewById(R.id.tabAll)
        tabInProgress = findViewById(R.id.tabInProgress)
        tabCompleted = findViewById(R.id.tabCompleted)
        recycler = findViewById(R.id.testsRecyclerViewLearning)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = TestsAdapter(listOf(), R.layout.item_test) { test ->
            startActivity(Intent(this, InfoTestActivity::class.java).apply {
                putExtra("TEST_ID", test.id)
                putExtra("TEST_NAME", test.name)
                putExtra("TEST_DURATION", test.durationMinutes)
                putExtra("TEST_Q_COUNT", test.questionsCount)
            })
        }
        recycler.adapter = adapter

        tabAll.setOnClickListener {
            selectTab(Tab.ALL)
        }
        tabInProgress.setOnClickListener {
            selectTab(Tab.IN_PROGRESS)
        }
        tabCompleted.setOnClickListener {
            selectTab(Tab.COMPLETED)
        }
        selectTab(Tab.ALL)
    }

    private enum class Tab { ALL, IN_PROGRESS, COMPLETED }
    private fun selectTab(tab: Tab) {
        tabAll.isSelected = (tab == Tab.ALL)
        tabInProgress.isSelected = (tab == Tab.IN_PROGRESS)
        tabCompleted.isSelected = (tab == Tab.COMPLETED)

        val items = when (tab) {
            Tab.ALL -> dbHelper.getAllTestItems()
            Tab.IN_PROGRESS -> dbHelper.getInProgressTestItems()
            Tab.COMPLETED -> dbHelper.getCompletedTestItems()
        }
        adapter.updateItems(items)
    }
}