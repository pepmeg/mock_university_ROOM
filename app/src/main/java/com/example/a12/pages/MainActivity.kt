package com.example.a12.pages

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope                            // ← импорт
import androidx.recyclerview.widget.LinearLayoutManager          // ← импорт
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.R
import com.example.a12.model.AppDatabase
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.TestItem
import com.example.a12.model.toTestItem
import com.example.a12.ui.TestsAdapter
import com.example.a12.utils.BottomNavHandler
import kotlinx.coroutines.launch                                  // ← импорт

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var dao: TestDao
    private lateinit var nav: BottomNavHandler
    private lateinit var card: FrameLayout

    private val infoClick: (TestItem) -> Unit = { test ->
        Intent(this, InfoTestActivity::class.java).apply {
            putExtra("TEST_ID", test.id)
            putExtra("TEST_NAME", test.name)
            putExtra("TEST_DURATION", test.durationMinutes)
            putExtra("TEST_Q_COUNT", test.questionsCount)
            startActivity(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getInstance(this)
        dao = db.testDao()
        nav = BottomNavHandler(this, findViewById(android.R.id.content)).also { it.setupNavigation() }
        card = findViewById(R.id.cardContainer)

        // Загружаем данные асинхронно
        lifecycleScope.launch {
            val items = dao.getAllTestItems().map { it.toTestItem() }

            listOf(
                R.id.testsRecyclerView,
                R.id.testsRecyclerView1,
                R.id.testsRecyclerView2
            ).forEach { id ->
                findViewById<RecyclerView>(id).apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = TestsAdapter(
                        items = items,
                        detailed = false,
                        dao = dao,
                        lifecycleOwner = this@MainActivity,  // ← передаём владельца для корутин внутри адаптера
                        onClick = infoClick,
                        onDelete = {}
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nav.setupNavigation()
        updateContinueCard()
    }

    private fun updateContinueCard() {
        lifecycleScope.launch {
            val lastResult = dao.getLastInProgressResult()
            if (lastResult != null) {
                // Получили агрегированные данные по тесту
                val testStats = dao.getTestWithStatsById(lastResult.testId)!!

                // Преобразуем в TestItem (используем ваш маппер)
                val testItem = testStats.toTestItem().copy(
                    // подставляем реальные данные из lastResult и testStats
                    resultId = lastResult.resultId,
                    remainingSeconds = lastResult.remainingSeconds?.toLong() ?: 0L,
                    status = lastResult.status,
                    finishedAt = lastResult.finishedAt
                )

                card.isVisible = true

                // Теперь можно спокойно брать iconResName и другие поля
                card.findViewById<ImageView>(R.id.cardIcon)
                    .setImageResource(
                        resources.getIdentifier(
                            testItem.iconResName,
                            "drawable",
                            packageName
                        )
                    )
                card.findViewById<TextView>(R.id.cardTitle).text = testItem.name

                // Получаем статистику ответов
                val userStats = dao.getResultStats(testItem.resultId)
                val answered = userStats.totalAnswers

                card.findViewById<TextView>(R.id.cardProgress).text =
                    "$answered/${testItem.questionsCount}"
                card.findViewById<TextView>(R.id.cardRemaining).text =
                    "${testItem.remainingSeconds / 60} min"

                card.findViewById<ProgressBar>(R.id.cardProgressBar).progress =
                    if (testItem.questionsCount > 0)
                        answered * 100 / testItem.questionsCount
                    else 0

                card.setOnClickListener {
                    Intent(this@MainActivity, TestActivity::class.java).apply {
                        putExtra("TEST_ID", testItem.id)
                        putExtra("RESULT_ID", testItem.resultId)
                        putExtra("REVIEW_MODE", false)
                        startActivity(this)
                    }
                }
            } else {
                card.isVisible = false
            }
        }
    }
}