package com.example.a12.model.entities

import androidx.room.*

@Entity(
    tableName = "test_results",
    foreignKeys = [ForeignKey(
        entity = TestEntity::class,
        parentColumns = ["test_id"],
        childColumns  = ["test_id"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("test_id")]
)
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("result_id") val resultId: Long = 0,
    @ColumnInfo("test_id") val testId: Long,
    @ColumnInfo("started_at") val startedAt: Long? = null,
    @ColumnInfo("finished_at") val finishedAt: Long? = null,
    @ColumnInfo("current_question_order") val currentQuestionOrder: Int = 1,
    @ColumnInfo("status") val status: String = "in_progress",
    @ColumnInfo("correct_percentage") val correctPercentage: Double = 0.0,
    @ColumnInfo("remaining_seconds") val remainingSeconds: Int? = null
)