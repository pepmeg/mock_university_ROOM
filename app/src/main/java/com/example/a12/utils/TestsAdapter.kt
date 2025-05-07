package com.example.a12.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a12.R
import com.example.a12.model.TestItem

class TestsAdapter(
    private val items: List<TestItem>,
    private val onClick: (TestItem) -> Unit
) : RecyclerView.Adapter<TestsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTv: TextView  = view.findViewById(R.id.testName)
        private val durTv: TextView   = view.findViewById(R.id.testDuration)
        private val iconIv: ImageView = view.findViewById(R.id.testIcon)

        fun bind(item: TestItem) {
            nameTv.text = item.name
            val h = item.durationMinutes / 60
            val m = item.durationMinutes % 60
            durTv.text = "${h}h ${m}min"
            // при желании: сменить iconIv.setImageResource(...)
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_card, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])
}