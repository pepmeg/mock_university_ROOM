package com.example.a12.utils.dots

import android.content.Context
import android.graphics.Color
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.a12.R
import com.example.a12.model.Question

fun setupQuestionNumberDots(
    questions: List<Question>,
    dotsContainer: LinearLayout,
    context: Context,
    onClick: (Int) -> Unit
) {
    dotsContainer.removeAllViews()
    val density = context.resources.displayMetrics.density
    val sizePx = (40 * density).toInt()
    val marginPx = (8 * density).toInt()

    questions.forEachIndexed { index, _ ->
        val dot = TextView(context).apply {
            text = (index + 1).toString()
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            background = ContextCompat.getDrawable(context, R.drawable.bg_circle_unselected)
            setTextColor(ContextCompat.getColor(context, R.color.text_unselected))
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                setMargins(0, 0, marginPx, 0)
            }
            setOnClickListener { onClick(index) }
        }
        dotsContainer.addView(dot)
    }
}

fun updateDotsUI(
    container: LinearLayout,
    currentIndex: Int,
    answeredSet: Set<Int>,
    context: Context
) {
    for (i in 0 until container.childCount) {
        val dot = container.getChildAt(i) as TextView
        when {
            i == currentIndex -> {
                dot.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_black)
                dot.setTextColor(Color.WHITE)
            }
            answeredSet.contains(i) -> {
                dot.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_selected)
                dot.setTextColor(Color.WHITE)
            }
            else -> {
                dot.background = ContextCompat.getDrawable(context, R.drawable.bg_circle_unselected)
                dot.setTextColor(ContextCompat.getColor(context, R.color.text_unselected))
            }
        }
    }
}

fun scrollToCurrentDot(
    dotsContainer: LinearLayout,
    scrollView: HorizontalScrollView,
    currentIndex: Int
) {
    val tv = dotsContainer.getChildAt(currentIndex)
    scrollView.post {
        val scrollX = tv.left - (scrollView.width - tv.width) / 2
        scrollView.smoothScrollTo(scrollX, 0)
    }
}