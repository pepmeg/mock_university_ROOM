package com.example.a12.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.a12.R
import com.example.a12.pages.LearningActivity
import com.example.a12.pages.MainActivity

class BottomNavHandler(
    private val activity: Activity,
    private val root: View
) {
    private val homeLayout = root.findViewById<LinearLayout>(R.id.homeLayout)
    private val examLayout = root.findViewById<LinearLayout>(R.id.examLayout)

    private val plashka0 = root.findViewById<ImageView>(R.id.plashka)
    private val plashka2 = root.findViewById<ImageView>(R.id.plashka2)

    private val homeIcon = root.findViewById<ImageView>(R.id.homeIcon)
    private val homeTxt  = root.findViewById<TextView>(R.id.homeText)
    private val examIcon = root.findViewById<ImageView>(R.id.examIcon)
    private val examTxt  = root.findViewById<TextView>(R.id.examText)

    private val inactive = Color.parseColor("#666666")
    private val active   = Color.parseColor("#7A5CFA")

    private val prefs = activity
        .getSharedPreferences("bottom_nav", Context.MODE_PRIVATE)

    fun setupNavigation() {
        hideAll()
        val selected = prefs.getString("selected_button", null)
        when {
            selected == "exam" && activity !is MainActivity -> activate(examLayout, plashka2, examIcon, examTxt)
            else                                             -> activate(homeLayout, plashka0, homeIcon, homeTxt)
        }
        homeLayout.setOnClickListener { selectTab("home") }
        examLayout.setOnClickListener { selectTab("exam") }
    }

    private fun selectTab(key: String) {
        prefs.edit().putString("selected_button", key).apply()
        hideAll()
        when (key) {
            "home" -> {
                activate(homeLayout, plashka0, homeIcon, homeTxt)
                if (activity !is MainActivity) {
                    activity.startActivity(
                        Intent(activity, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                }
            }
            "exam" -> {
                activate(examLayout, plashka2, examIcon, examTxt)
                activity.startActivity(Intent(activity, LearningActivity::class.java))
            }
        }
    }

    private fun hideAll() {
        listOf(plashka0, plashka2).forEach { it.visibility = View.GONE }
        listOf(homeIcon, examIcon).forEach { it.setColorFilter(inactive) }
        listOf(homeTxt, examTxt).forEach { it.setTextColor(inactive) }
    }

    private fun activate(
        layout: LinearLayout,
        plashka: ImageView,
        icon: ImageView,
        text: TextView
    ) {
        plashka.visibility = View.VISIBLE
        icon.setColorFilter(active)
        text.setTextColor(active)
    }
}