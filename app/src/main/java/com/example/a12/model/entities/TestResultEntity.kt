package com.example.a12.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "result_id")
    val resultId: Long = 0L,
    @ColumnInfo(name = "test_id")
    val testId: Long,
    val status: String,
    @ColumnInfo(name = "current_question_order")
    val currentQuestionOrder: Int,
    @ColumnInfo(name = "remaining_seconds")
    val remainingSeconds: Int?,
    @ColumnInfo(name = "finished_at")
    val finishedAt: Long?,
    @ColumnInfo(name = "correct_percentage")
    val correctPercentage: Double,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000
)