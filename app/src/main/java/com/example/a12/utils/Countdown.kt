package com.example.a12.utils.countdown

import android.content.Context
import android.os.CountDownTimer
import android.widget.TextView

fun startCountdown(
    minutes: Int,
    timerText: TextView,
    context: Context,
    onFinish: () -> Unit
): CountDownTimer {
    val totalMillis = minutes * 60_000L
    return object : CountDownTimer(totalMillis, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val m = millisUntilFinished / 60000
            val s = (millisUntilFinished % 60000) / 1000
            timerText.text = String.format("%d:%02d", m, s)
        }

        override fun onFinish() {
            timerText.text = "0:00"
            onFinish()
        }
    }.start()
}