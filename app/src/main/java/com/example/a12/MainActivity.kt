package com.example.a12

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.model.TestItem
import com.example.a12.ui.TestsAdapter
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private lateinit var recycler: RecyclerView
    private lateinit var navHandler: BottomNavHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Очистка и повторное копирование БД из assets
        ensureDatabaseCopied()

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

        // После того как база скопирована, создаём DbHelper
        dbHelper = DbHelper(this)
        val tests: List<TestItem> = dbHelper.getAllTestItems()

        // Устанавливаем адаптер и передаём в Intent все параметры
        recycler.adapter = TestsAdapter(
            items       = tests,
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
        // Восстанавливаем выделение в нижней панели
        val prefs = getSharedPreferences("bottom_nav", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_button", "home").apply()
        navHandler.setupNavigation()
    }

    /**
     * Удаляет существующую копию БД и заново копирует её из assets.
     * Вызывать один раз при старте приложения, до первого доступа к БД.
     */
    private fun ensureDatabaseCopied() {
        val dbName = "mock_university.db"
        val dbFile = applicationContext.getDatabasePath(dbName)

        // 1) Удаляем старый файл и WAL/SHM
        if (dbFile.exists()) {
            dbFile.delete()
        }
        File(dbFile.absolutePath + "-wal").delete()
        File(dbFile.absolutePath + "-shm").delete()

        // 2) Копируем свежую БД из assets
        dbFile.parentFile?.mkdirs()
        applicationContext.assets.open(dbName).use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}