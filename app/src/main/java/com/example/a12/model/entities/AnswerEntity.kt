package com.example.a12.model.entities

import androidx.room.*

@Entity(
    tableName = "answers"
)
data class AnswerEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("answer_id")  val answerId: Int = 0,
    @ColumnInfo("question_id")                                 val questionId: Int,
    @ColumnInfo("answer_text")                                 val answerText: String,
    @ColumnInfo("is_correct")                                  val isCorrect: Boolean = false
)