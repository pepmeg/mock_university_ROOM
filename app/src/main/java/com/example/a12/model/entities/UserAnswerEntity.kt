package com.example.a12.model.entities

import androidx.room.*

@Entity(
    tableName = "user_answers",
    foreignKeys = [
        ForeignKey(
            entity = TestResultEntity::class,
            parentColumns = ["result_id"],
            childColumns  = ["result_id"],
            onDelete      = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["question_id"],
            childColumns  = ["question_id"]
        ),
        ForeignKey(
            entity = AnswerEntity::class,
            parentColumns = ["answer_id"],
            childColumns  = ["answer_id"]
        )
    ],
    indices = [Index("result_id"), Index("question_id"), Index("answer_id")]
)
data class UserAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("user_answer_id")
    val userAnswerId: Long = 0,

    @ColumnInfo("result_id")
    val resultId: Long,

    @ColumnInfo("question_id")
    val questionId: Long,

    @ColumnInfo("answer_id")
    val answerId: Long?,

    @ColumnInfo("free_text_answer")
    val freeTextAnswer: String?,

    @ColumnInfo("is_correct")
    val isCorrect: Boolean
)