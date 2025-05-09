package com.example.a12.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.R
import com.example.a12.model.TestItem

class TestsAdapter(
    private val items: List<TestItem>,
    private val layoutResId: Int,
    private val onClick: (TestItem) -> Unit
) : RecyclerView.Adapter<TestsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val iconIv: ImageView = view.findViewById(R.id.testIcon)
        private val nameTv: TextView  = view.findViewById(R.id.testName)

        // ищем оба возможных ID
        private val countTv: TextView? = view.findViewById(R.id.questionsCount)
            ?: view.findViewById(R.id.questionsCount)

        private val durTv: TextView? = view.findViewById(R.id.duration)
            ?: view.findViewById(R.id.testDuration)

        private val continueBtn: FrameLayout? = view.findViewById(R.id.btn_continue_test)

        fun bind(item: TestItem) {
            iconIv.setImageResource(R.drawable.java_logo)
            nameTv.text = item.name

            // только если нашли
            countTv?.text = "${item.answeredCount}/${item.questionsCount}"
            durTv?.text   = "${item.durationMinutes / 60}h ${item.durationMinutes % 60}"

            // кликаем по всему itemView или по кнопке
            itemView.setOnClickListener { onClick(item) }
            continueBtn?.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])
}