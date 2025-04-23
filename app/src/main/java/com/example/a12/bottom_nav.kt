package com.example.a12

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout

class BottomNavHandler(private val activity: Activity, private val rootView: View) {

    fun setupNavigation() {
        val examButton = rootView.findViewById<LinearLayout>(R.id.examLayout)

        examButton.setOnClickListener {
            val intent = Intent(activity, LearningActivity::class.java)
            activity.startActivity(intent)
        }
    }
}