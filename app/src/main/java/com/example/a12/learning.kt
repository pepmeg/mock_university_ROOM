package com.example.a12

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.model.TestItem
import com.example.a12.ui.TestsAdapter

class LearningActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learning)

        BottomNavHandler(this, findViewById(android.R.id.content)).setupNavigation()

        dbHelper = DbHelper(this)
        recycler = findViewById(R.id.testsRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)

        // Получаем все тесты из БД
        val tests: List<TestItem> = dbHelper.getAllTestItems()

        recycler.adapter = TestsAdapter(tests) { test ->
            val intent = Intent(this, InfoTestActivity::class.java).apply {
                putExtra("TEST_ID",          test.id)
                putExtra("TEST_NAME",        test.name)
                putExtra("TEST_DURATION",    test.durationMinutes)
                putExtra("TEST_Q_COUNT",     test.questionsCount)
            }
            startActivity(intent)
        }
    }
}