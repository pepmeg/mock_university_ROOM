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
    private var items: List<TestItem>,
    private val layoutResId: Int,
    private val onClick: (TestItem) -> Unit
) : RecyclerView.Adapter<TestsAdapter.ViewHolder>() {

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val iconIv   : ImageView     = v.findViewById(R.id.testIcon)
        private val nameTv   : TextView      = v.findViewById(R.id.testName)
        private val countTv  : TextView?     = v.findViewById(R.id.questionsCount)
        private val durTv    : TextView?     = v.findViewById(R.id.testDuration)
        private val remTv    : TextView?     = v.findViewById(R.id.remainingTime)
        private val continueBtn: FrameLayout?= v.findViewById(R.id.btn_continue_test)
        private val continueBtnBg: View?      = v.findViewById(R.id.continueButtonBg)
        private val continueBtnText: TextView?= v.findViewById(R.id.continueButtonText)

        fun bind(item: TestItem) {
            val ctx = iconIv.context
            val rid = ctx.resources.getIdentifier(item.iconResName, "drawable", ctx.packageName)
            if (rid != 0) iconIv.setImageResource(rid)

            nameTv.text   = item.name
            countTv?.text = "${item.answeredCount}/${item.questionsCount}"
            durTv?.text   = "${item.durationMinutes/60}h ${item.durationMinutes%60}min"
            remTv?.text   = "${item.remainingSeconds/60}min"

            continueBtnBg?.let { bg ->
                if (item.status == "completed") {
                    bg.setBackgroundResource(R.drawable.button_revisit)
                    continueBtnText?.visibility = View.GONE
                } else {
                    bg.setBackgroundResource(R.drawable.bg_button)
                    continueBtnText?.visibility = View.VISIBLE
                }
                bg.setOnClickListener { onClick(item) }
            }

            itemView.setOnClickListener    { onClick(item) }
            continueBtn?.setOnClickListener{ onClick(item) }
        }
    }

    fun updateItems(newItems: List<TestItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context)
            .inflate(layoutResId, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])
}