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
    answeredCount    = 0,
    remainingSeconds = 0L,
    status           = lastStatus ?: "in_progress",
    iconResName      = "ic_test_default",
    finishedAt       = null
)