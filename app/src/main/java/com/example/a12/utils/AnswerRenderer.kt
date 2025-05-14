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

    answers.forEach { answer ->
        val isLong = answer.text.length > 50
        val rb = RadioButton(context).apply {
            text = answer.text
            id = answer.id
            buttonDrawable = null
            background = ContextCompat.getDrawable(
                context,
                if (isLong) R.drawable.bg_answer_neutral_long else R.drawable.bg_answer_neutral_short
            )
            setPadding(dpToPx(context,16), dpToPx(context,16), dpToPx(context,16), dpToPx(context,16))
            layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0,0,0,dpToPx(context,12))
            }
        }
        answersGroup.addView(rb)
    }

    if (selectedAnswerId != null) {
        answersGroup.findViewById<RadioButton>(selectedAnswerId)?.isChecked = true
        updateRadioButtonsBackgrounds(answersGroup, selectedAnswerId, context)
    } else {
        updateRadioButtonsBackgrounds(answersGroup, -1, context)
    }

    answersGroup.setOnCheckedChangeListener { group, id ->
        updateRadioButtonsBackgrounds(group, id, context)
        onAnswerSelected(questionId, id)
    }
}

fun updateRadioButtonsBackgrounds(group: RadioGroup, selectedId: Int, context: Context) {
    for (i in 0 until group.childCount) {
        val rb = group.getChildAt(i) as RadioButton
        val isLong = rb.text.length > 50
        val bgRes = if (rb.id == selectedId) {
            if (isLong) R.drawable.bg_answer_selected_long else R.drawable.bg_answer_selected_short
        } else {
            if (isLong) R.drawable.bg_answer_neutral_long else R.drawable.bg_answer_neutral_short
        }
        rb.background = ContextCompat.getDrawable(context, bgRes)
    }
}

private fun dpToPx(c: Context, dp: Int) =
    (dp * c.resources.displayMetrics.density).toInt()