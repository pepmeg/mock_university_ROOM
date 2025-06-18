package com.example.a12.model.entities

import androidx.room.*

@Entity(
    tableName = "user_answers"
)
data class UserAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_answer_id")
    val userAnswerId: Long = 0,
    @ColumnInfo(name = "result_id")
    val resultId: Long,
    @ColumnInfo(name = "question_id")
    val questionId: Long,
    @ColumnInfo(name = "answer_id")
    val answerId: Long?,
    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean
)