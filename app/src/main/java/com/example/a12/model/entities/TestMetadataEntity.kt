package com.example.a12.model.entities

import androidx.room.*

@Entity(
    tableName = "test_metadata",
    foreignKeys = [ForeignKey(
        entity = TestEntity::class,
        parentColumns = ["test_id"],
        childColumns  = ["test_id"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("test_id")]
)
data class TestMetadataEntity(
    @PrimaryKey @ColumnInfo("test_id")            val testId: Int,
    @ColumnInfo("average_success_rate")           val averageSuccessRate: Double = 0.0,
    @ColumnInfo("total_attempts")                 val totalAttempts: Int = 0
)