package com.example.a12.model

import com.example.a12.model.DAO.TestDao

data class TestItem(
    val id: Long,
    val resultId: Long,
    val name: String,
    val durationMinutes: Int,
    val questionsCount: Int,
    val answeredCount: Int,
    val remainingSeconds: Long,
    val status: String,
    val iconResName: String,
    val finishedAt: Long?
)

fun TestDao.TestWithStats.toTestItem(): TestItem = TestItem(
    id               = test.testId,
    resultId         = lastResultId ?: 0L,
    name             = test.testName,
    durationMinutes  = test.durationMinutes,
    questionsCount   = totalQuestions,
    answeredCount    = 0,          // при необходимости можно взять из других полей
    remainingSeconds = 0L,         // аналогично
    status           = lastStatus ?: "in_progress",
    iconResName      = "ic_test_default",  // или другое значение по умолчанию
    finishedAt       = null        // если нет данных
)