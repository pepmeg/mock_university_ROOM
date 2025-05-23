package com.example.a12.model.entities

import androidx.room.*

@Entity(
    tableName = "tests"
)
data class TestEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("test_id") val testId: Long = 0L,
    @ColumnInfo("test_name") val testName: String,
    @ColumnInfo("duration_minutes") val durationMinutes: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)