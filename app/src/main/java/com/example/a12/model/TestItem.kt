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

fun TestDao.TestWithStats.toTestItem(): TestItem {
    val iconRes = when {
        test.testName.contains("Java",   ignoreCase = true) -> "java_logo"
        test.testName.contains("C++",    ignoreCase = true) -> "c_logo"
        test.testName.contains("React",  ignoreCase = true) -> "react_logo"
        else                                               -> "ic_test_default"
    }

    return TestItem(
        id               = test.testId,
        resultId         = lastResultId ?: 0L,
        name             = test.testName,
        durationMinutes  = test.durationMinutes,
        questionsCount   = totalQuestions,
        answeredCount    = answeredCount,
        remainingSeconds = remainingSeconds.toLong(),
        status           = lastStatus ?: "in_progress",
        iconResName      = iconRes,
        finishedAt       = finishedAt
    )
}
