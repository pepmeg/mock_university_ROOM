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
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.R
import com.example.a12.model.DAO.TestDao
import com.example.a12.model.TestItem
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.Locale

class TestsAdapter(
    private var items: List<TestItem>,
    private val detailed: Boolean,
    private val dao: TestDao,
    private val lifecycleOwner: LifecycleOwner,
    private val onClick: (TestItem) -> Unit,
    private val onDelete: (TestItem) -> Unit
) : RecyclerView.Adapter<TestsAdapter.BaseVH>() {

    companion object {
        private const val TYPE_SIMPLE = 0
        private const val TYPE_DETAILED = 1
    }

    abstract inner class BaseVH(view: View) : RecyclerView.ViewHolder(view) {
        protected fun loadIcon(iconView: ImageView, name: String) {
            iconView.context.resources.getIdentifier(name, "drawable", iconView.context.packageName)
                .takeIf { it != 0 }
                ?.let(iconView::setImageResource)
        }
        abstract fun bind(item: TestItem)
    }

    inner class SimpleVH(view: View) : BaseVH(view) {
        private val icon     = view.findViewById<ImageView>(R.id.testIcon)
        private val name     = view.findViewById<TextView>(R.id.testName)
        private val duration = view.findViewById<TextView>(R.id.testDuration)

        override fun bind(item: TestItem) {
            loadIcon(icon, item.iconResName)
            name.text     = item.name
            duration.text = "${item.durationMinutes/60}h ${item.durationMinutes%60}m"
            itemView.setOnClickListener { onClick(item) }
        }
    }

    inner class DetailedVH(view: View) : BaseVH(view) {
        private val icon      = view.findViewById<ImageView>(R.id.testIcon)
        private val name      = view.findViewById<TextView>(R.id.testName)
        private val count     = view.findViewById<TextView>(R.id.questionsCount)
        private val extra     = view.findViewById<TextView>(R.id.remainingTime)
        private val btnBg     = view.findViewById<View>(R.id.continueButtonBg)
        private val btnText   = view.findViewById<TextView>(R.id.continueButtonText)
        private val trash     = view.findViewById<ImageView>(R.id.delete)
        private val colorP    = ContextCompat.getColor(view.context, R.color.purple)
        private val colorG    = ContextCompat.getColor(view.context, R.color.gray)

        override fun bind(item: TestItem) {
            loadIcon(icon, item.iconResName)
            name.text = item.name

            lifecycleOwner.lifecycleScope.launch {
                val stats = dao.getResultStats(item.resultId)
                if (item.status == "completed") {
                    val prefix = "Score: "
                    val result = "${stats.correctAnswers}/${item.questionsCount}"
                    val span = SpannableStringBuilder().apply {
                        append(prefix, ForegroundColorSpan(colorG), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        append(result, ForegroundColorSpan(colorP), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    count.text = span
                    extra.formatDate(item.finishedAt ?: 0L, colorG)
                    btnBg.setBackgroundResource(R.drawable.button_revisit)
                    btnText.isVisible = false
                    trash.setOnClickListener { onDelete(item) }
                } else {
                    count.setColoredText(
                        "${item.answeredCount}/${item.questionsCount}" to " questions",
                        colorG, colorP
                    )
                    extra.setColoredText("${item.remainingSeconds/60}min" to " remaining", colorG, colorP)
                    btnBg.setBackgroundResource(R.drawable.bg_button)
                    btnText.isVisible = true
                    trash.setOnClickListener(null)
                }
                btnBg.setOnClickListener { onClick(item) }
                itemView.setOnClickListener { onClick(item) }
            }
        }
    }

    override fun getItemViewType(pos: Int) =
        if (detailed) TYPE_DETAILED else TYPE_SIMPLE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH {
        val inflater = LayoutInflater.from(parent.context)
        val layout = if (viewType == TYPE_SIMPLE) R.layout.item_test_card else R.layout.item_test
        val vh = if (viewType == TYPE_SIMPLE)
            SimpleVH(inflater.inflate(layout, parent, false))
        else
            DetailedVH(inflater.inflate(layout, parent, false))
        return vh
    }

    override fun onBindViewHolder(holder: BaseVH, pos: Int) {
        val item = items[pos]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<TestItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun TextView.setColoredText(
        prefix: String = "",
        colored: Pair<Any, Any>,
        @ColorInt c1: Int,
        @ColorInt c2: Int
    ) {
        val (first, second) = colored
        val sb = SpannableStringBuilder()
        sb.append(prefix, ForegroundColorSpan(c1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.append(first.toString(), ForegroundColorSpan(c2), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.append(second.toString(), ForegroundColorSpan(c1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        text = sb
    }

    private fun TextView.setColoredText(
        pair: Pair<String, String>,
        @ColorInt cPrimary: Int,
        @ColorInt cSecondary: Int
    ) = setColoredText(prefix = "", colored = pair, cPrimary, cSecondary)

    private fun TextView.formatDate(timeMs: Long, @ColorInt color: Int) {
        val day = Calendar.getInstance().apply { timeInMillis = timeMs }
            .get(Calendar.DAY_OF_MONTH)
        val suf = when {
            day in 11..13 -> "th"
            day%10==1 -> "st"
            day%10==2 -> "nd"
            day%10==3 -> "rd"
            else -> "th"
        }
        val sdf = SimpleDateFormat("MMMM d'$suf', yyyy", Locale.getDefault())
        text = sdf.format(Date(timeMs))
        setTextColor(color)
    }
}