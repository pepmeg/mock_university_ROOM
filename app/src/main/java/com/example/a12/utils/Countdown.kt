package com.example.a12.utils.countdown

import android.os.CountDownTimer
import android.widget.TextView
import com.example.a12.model.DbHelper

fun startCountdown(
    initialMillis: Long,
    timerText: TextView,
    resultId: Long,
    dbHelper: DbHelper,
    onFinish: () -> Unit
): CountDownTimer {
    return object : CountDownTimer(initialMillis, 1_000) {
        override fun onTick(millisUntilFinished: Long) {
            val m = millisUntilFinished / 60_000
            val s = (millisUntilFinished % 60_000) / 1_000
            timerText.text = String.format("%d:%02d", m, s)
            dbHelper.updateRemainingTime(resultId, (millisUntilFinished / 1_000).toInt())
        }
        override fun onFinish() {
            timerText.text = "0:00"
            onFinish()
        }
    }.start()
}
