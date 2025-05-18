package com.example.a12.model.entities

import androidx.room.*

@Entity(
    tableName = "questions",
    foreignKeys = [ForeignKey(
        entity = TestEntity::class,
        parentColumns = ["test_id"],
        childColumns = ["test_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("test_id"),
        Index(value = ["test_id","order_number"], unique = true)
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "question_id") val questionId: Int = 0,
    @ColumnInfo(name = "test_id") val testId: Int,
    @ColumnInfo(name = "question_text") val questionText: String,
    @ColumnInfo(name = "question_type") val questionType: String,
    @ColumnInfo(name = "min_answers") val minAnswers: Int = 1,
    @ColumnInfo(name = "max_answers") val maxAnswers: Int = 1,
    @ColumnInfo(name = "order_number") val orderNumber: Int
)