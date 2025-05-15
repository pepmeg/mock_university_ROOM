package com.example.a12.ui

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.R
import com.example.a12.model.TestItem
import java.sql.Date
import java.util.Locale

class TestsAdapter(
    private var items: List<TestItem>,
    private val detailedLayout: Boolean,
    private val onClick: (TestItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SIMPLE = 0
        private const val TYPE_DETAILED = 1
    }

    inner class SimpleVH(v: View) : RecyclerView.ViewHolder(v) {
        private val icon     : ImageView = v.findViewById(R.id.testIcon)
        private val name     : TextView  = v.findViewById(R.id.testName)
        private val duration : TextView  = v.findViewById(R.id.testDuration)

        fun bind(item: TestItem) {
            val ctx = icon.context
            ctx.resources.getIdentifier(item.iconResName, "drawable", ctx.packageName)
                .takeIf { it != 0 }
                ?.let(icon::setImageResource)

            name.text = item.name
            duration.text = "${item.durationMinutes / 60}h ${item.durationMinutes % 60}min"

            itemView.setOnClickListener { onClick(item) }
        }
    }

    inner class DetailedVH(v: View) : RecyclerView.ViewHolder(v) {
        private val icon    : ImageView = v.findViewById(R.id.testIcon)
        private val name    : TextView  = v.findViewById(R.id.testName)
        private val count   : TextView  = v.findViewById(R.id.questionsCount)
        private val extra   : TextView  = v.findViewById(R.id.remainingTime)
        private val btnBg   : View      = v.findViewById(R.id.continueButtonBg)
        private val btnText : TextView  = v.findViewById(R.id.continueButtonText)

        private val purple = ContextCompat.getColor(itemView.context, R.color.purple)
        private val gray   = ContextCompat.getColor(itemView.context, R.color.gray)

        fun bind(item: TestItem) {
            val ctx = icon.context
            ctx.resources.getIdentifier(item.iconResName, "drawable", ctx.packageName)
                .takeIf { it != 0 }
                ?.let(icon::setImageResource)

            name.text = item.name

            if (item.status == "completed" && item.finishedAt != null) {
                val prefix = "Score: "
                val result = "${item.answeredCount}/${item.questionsCount}"
                val span = SpannableStringBuilder()
                    .append(prefix, ForegroundColorSpan(gray), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append(result, ForegroundColorSpan(purple), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                count.text = span

                val suf = daySuffix(item.finishedAt)
                val df  = SimpleDateFormat("MMMM d'$suf', yyyy", Locale.getDefault())
                extra.text = df.format(Date(item.finishedAt))
                extra.setTextColor(gray)

                btnBg.setBackgroundResource(R.drawable.button_revisit)
                btnText.visibility = View.GONE

            } else {
                val qty = "${item.answeredCount}/${item.questionsCount}"
                val label = if (item.questionsCount == 1) " question" else " questions"
                val spanCount = SpannableStringBuilder()
                    .append(qty, ForegroundColorSpan(purple), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append(label, ForegroundColorSpan(gray), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                count.text = spanCount

                val mins = (item.remainingSeconds / 60).toInt()
                val timeQty = "$mins min"
                val timeLabel = " remaining"
                val spanTime = SpannableStringBuilder()
                    .append(timeQty, ForegroundColorSpan(purple), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append(timeLabel, ForegroundColorSpan(gray), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                extra.text = spanTime

                btnBg.setBackgroundResource(R.drawable.bg_button)
                btnText.visibility = View.VISIBLE
            }

            btnBg.setOnClickListener { onClick(item) }
            itemView.setOnClickListener { onClick(item) }
        }

        private fun daySuffix(timeMs: Long): String {
            val d = Calendar.getInstance().apply { timeInMillis = timeMs }
                .get(Calendar.DAY_OF_MONTH)
            return when {
                d in 11..13 -> "th"
                d % 10 == 1 -> "st"
                d % 10 == 2 -> "nd"
                d % 10 == 3 -> "rd"
                else        -> "th"
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (detailedLayout) TYPE_DETAILED else TYPE_SIMPLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SIMPLE) {
            SimpleVH(inflater.inflate(R.layout.item_test_card, parent, false))
        } else {
            DetailedVH(inflater.inflate(R.layout.item_test, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        val item = items[pos]
        when (holder) {
            is SimpleVH   -> holder.bind(item)
            is DetailedVH -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<TestItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}