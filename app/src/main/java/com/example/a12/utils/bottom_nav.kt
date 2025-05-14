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

class BottomNavHandler(private val activity: Activity, private val rootView: View) {

    fun setupNavigation() {

        val examBtn  = rootView.findViewById<LinearLayout>(R.id.examLayout)
        val examTxt  = rootView.findViewById<TextView>(R.id.examText)
        val examIcon = rootView.findViewById<ImageView>(R.id.examIcon)

        val homeBtn  = rootView.findViewById<LinearLayout>(R.id.homeLayout)
        val homeTxt  = rootView.findViewById<TextView>(R.id.homeText)
        val homeIcon = rootView.findViewById<ImageView>(R.id.homeIcon)

        val defColor = Color.parseColor("#666666")
        val selColor = Color.parseColor("#7A5CFA")

        examTxt.setTextColor(defColor)
        examIcon.setColorFilter(defColor)
        homeTxt.setTextColor(defColor)
        homeIcon.setColorFilter(defColor)

        val prefs    = activity.getSharedPreferences("bottom_nav", Context.MODE_PRIVATE)
        val selected = prefs.getString("selected_button", null)

        when (selected) {
            "exam" -> {
                examTxt.setTextColor(selColor)
                examIcon.setColorFilter(selColor)
            }
            "home" -> {
                homeTxt.setTextColor(selColor)
                homeIcon.setColorFilter(selColor)
            }
        }

        examBtn.setOnClickListener {
            prefs.edit().putString("selected_button", "exam").apply()
            examTxt.setTextColor(selColor)
            examIcon.setColorFilter(selColor)
            activity.startActivity(Intent(activity, LearningActivity::class.java))
        }

        homeBtn.setOnClickListener {
            prefs.edit().putString("selected_button", "home").apply()
            homeTxt.setTextColor(selColor)
            homeIcon.setColorFilter(selColor)
            if (activity !is MainActivity) {
                activity.startActivity(
                    Intent(activity, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            }
        }
    }
}