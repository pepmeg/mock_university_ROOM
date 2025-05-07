package com.example.a12

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.model.TestItem
import com.example.a12.ui.TestsAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private lateinit var recycler: RecyclerView
    private lateinit var navHandler: BottomNavHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация навигации
        navHandler = BottomNavHandler(this, findViewById(android.R.id.content))
        navHandler.setupNavigation()

        // RecyclerView
        recycler = findViewById(R.id.testsRecyclerView)
        recycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )

        // Получаем все тесты — в каждом TestItem уже есть questionsCount
        dbHelper = DbHelper(this)
        val tests: List<TestItem> = dbHelper.getAllTestItems()

        // Устанавливаем адаптер и передаём в Intent все четыре параметра
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

    override fun onResume() {
        super.onResume()
        // Восстанавливаем выделение в нижней панеле
        val prefs = getSharedPreferences("bottom_nav", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_button", "home").apply()
        navHandler.setupNavigation()
    }
}
