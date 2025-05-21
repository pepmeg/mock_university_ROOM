package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.R
import com.example.a12.model.AppDatabase
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.TestItem
import com.example.a12.model.toTestItem
import com.example.a12.ui.TestsAdapter
import com.example.a12.utils.BottomNavHandler
import kotlinx.coroutines.launch

class LearningActivity : AppCompatActivity() {

    private lateinit var adapter: TestsAdapter
    private lateinit var dao: TestDao
    private var currentTab = Tab.ALL

    private val tabs by lazy {
        listOf(
            R.id.tabAll to Tab.ALL,
            R.id.tabInProgress to Tab.IN_PROGRESS,
            R.id.tabCompleted to Tab.COMPLETED
        )
    }

    private val recycler by lazy { findViewById<RecyclerView>(R.id.testsRecyclerViewLearning) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learning)

        BottomNavHandler(this, findViewById(android.R.id.content)).setupNavigation()
        dao = AppDatabase.getInstance(this).testDao()

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TestsAdapter(
            items = emptyList(),
            detailed = true,
            onClick = ::openInfo,
            onDelete = ::deleteAndRefresh,
            dao = dao,
            lifecycleOwner = this
        )
        recycler.adapter = adapter

        tabs.forEach { (id, tab) ->
            findViewById<FrameLayout>(id).setOnClickListener { selectTab(tab) }
        }

        selectTab(Tab.ALL)
    }

    private fun openInfo(item: TestItem) {
        startActivity(Intent(this, InfoTestActivity::class.java).apply {
            putExtra("TEST_ID", item.id)
            putExtra("TEST_NAME", item.name)
            putExtra("TEST_DURATION", item.durationMinutes)
            putExtra("TEST_Q_COUNT", item.questionsCount)
        })
    }

    private fun deleteAndRefresh(item: TestItem) {
        lifecycleScope.launch {
            dao.deleteTestResults(item.id)
            selectTab(currentTab)
        }
    }

    private fun selectTab(tab: Tab) {
        currentTab = tab
        tabs.forEach { (id, t) ->
            findViewById<FrameLayout>(id).isSelected = (t == tab)
        }

        lifecycleScope.launch {
            val testItems = when (tab) {
                Tab.ALL -> dao.getAllTestItems()
                Tab.IN_PROGRESS -> dao.getAllTestItems().filter { it.lastStatus == "in_progress" }
                Tab.COMPLETED -> dao.getAllTestItems().filter { it.lastStatus == "completed" }
            }.map { it.toTestItem() }

            adapter.updateItems(testItems)
        }
    }

    private enum class Tab { ALL, IN_PROGRESS, COMPLETED }
}