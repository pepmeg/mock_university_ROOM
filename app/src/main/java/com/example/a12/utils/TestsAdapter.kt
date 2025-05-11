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

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val iconIv   : ImageView   = v.findViewById(R.id.testIcon)
        private val nameTv   : TextView    = v.findViewById(R.id.testName)
        private val countTv  : TextView?   = v.findViewById(R.id.questionsCount)
        private val durTv    : TextView?   = v.findViewById(R.id.testDuration)
        private val remTv    : TextView?   = v.findViewById(R.id.remainingTime)
        private val continueBtn: FrameLayout? = v.findViewById(R.id.btn_continue_test)

        fun bind(item: TestItem) {
            // Иконка по имени ресурса
            val ctx = iconIv.context
            val rid = ctx.resources.getIdentifier(
                item.iconResName, "drawable", ctx.packageName
            )
            if (rid != 0) iconIv.setImageResource(rid)

            nameTv.text  = item.name
            countTv?.text= "${item.answeredCount}/${item.questionsCount}"
            durTv?.text  = "${item.durationMinutes/60}h ${item.durationMinutes%60}min"
            remTv?.text  = "%dmin"
                .format(item.remainingSeconds/60, item.remainingSeconds%60)

            itemView.setOnClickListener    { onClick(item) }
            continueBtn?.setOnClickListener{ onClick(item) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        ViewHolder(LayoutInflater.from(p.context)
            .inflate(layoutResId, p, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(h: ViewHolder, pos: Int) =
        h.bind(items[pos])
}