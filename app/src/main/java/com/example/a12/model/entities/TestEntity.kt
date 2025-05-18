package com.example.a12.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tests",
    indices = [Index(value = ["test_name"], unique = false)]
)
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "test_id")
    val testId: Long = 0,

    @ColumnInfo(name = "test_name")
    val testName: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)