package com.example.a12.model

data class TestItem(
    val id: Int,
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