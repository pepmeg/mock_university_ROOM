package com.example.a12.utils.answers

import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import com.example.a12.R
import com.example.a12.model.Answer

fun renderAnswers(
    context: Context,
    answersGroup: RadioGroup,
    answers: List<Answer>,
    questionId: Int,
    selectedAnswerId: Int?,
    onAnswerSelected: (questionId: Int, answerId: Int) -> Unit
) {
    answersGroup.setOnCheckedChangeListener(null)
    answersGroup.removeAllViews()

    // Создаём и добавляем RadioButton'ы
    answers.forEach { answer ->
        val isLong = answer.text.length > 50
        val rb = RadioButton(context).apply {
            text = answer.text
            id = answer.id  // ID ответа используется как ID кнопки — ОБЯЗАТЕЛЬНО
            buttonDrawable = null
            background = ContextCompat.getDrawable(
                context,
                if (isLong) R.drawable.bg_answer_neutral_long else R.drawable.bg_answer_neutral_short
            )
            setPadding(
                dpToPx(context, 16),
                dpToPx(context, 16),
                dpToPx(context, 16),
                dpToPx(context, 16)
            )
            layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(context, 12))
            }
        }
        answersGroup.addView(rb)
    }

    // Выставляем сохранённый ответ, если есть
    if (selectedAnswerId != null) {
        val targetButton = answersGroup.findViewById<RadioButton>(selectedAnswerId)
        targetButton?.isChecked = true
        updateRadioButtonsBackgrounds(answersGroup, selectedAnswerId, context)
    } else {
        updateRadioButtonsBackgrounds(answersGroup, -1, context) // ничего не выбрано
    }

    // Назначаем слушатель
    answersGroup.setOnCheckedChangeListener { group, checkedId ->
        updateRadioButtonsBackgrounds(group, checkedId, context)
        onAnswerSelected(questionId, checkedId)
    }
}

private fun updateRadioButtonsBackgrounds(group: RadioGroup, checkedId: Int, context: Context) {
    for (i in 0 until group.childCount) {
        val child = group.getChildAt(i) as RadioButton
        val isLong = child.text.length > 50
        val bgRes = if (child.id == checkedId) {
            if (isLong) R.drawable.bg_answer_selected_long else R.drawable.bg_answer_selected_short
        } else {
            if (isLong) R.drawable.bg_answer_neutral_long else R.drawable.bg_answer_neutral_short
        }
        child.background = ContextCompat.getDrawable(context, bgRes)
    }
}

private fun dpToPx(context: Context, dp: Int): Int =
    (dp * context.resources.displayMetrics.density).toInt()