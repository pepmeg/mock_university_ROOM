package com.example.a12.model.entities

import androidx.room.*

@Entity(
    tableName = "tests",
    indices = [ Index(value = ["test_name"], name = "index_tests_test_name") ]
)
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "test_id")
    val testId: Long = 0L,

    @ColumnInfo(name = "test_name")
    val testName: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(
        name = "created_at",
        defaultValue = "CURRENT_TIMESTAMP"
    )
    val createdAt: Long = System.currentTimeMillis()
)