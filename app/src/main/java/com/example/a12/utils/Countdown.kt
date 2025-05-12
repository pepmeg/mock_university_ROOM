package com.example.a12.utils.countdown

import android.content.Context
import android.os.CountDownTimer
import android.widget.TextView
import com.example.a12.DbHelper

private lateinit var dbHelper: DbHelper

fun startCountdown(
    minutes: Int,
    timerText: TextView,
    context: Context,
    resultId: Long,
    dbHelper: DbHelper,
    onFinish: () -> Unit
): CountDownTimer {
    val totalMillis = minutes * 60_000L
    return object : CountDownTimer(totalMillis, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            // Обновляем UI
            val m = millisUntilFinished / 60000
            val s = (millisUntilFinished % 60000) / 1000
            timerText.text = String.format("%d:%02d", m, s)
            // Сохраняем оставшееся время
            dbHelper.updateRemainingTime(resultId, (millisUntilFinished / 1000).toInt())
        }
        override fun onFinish() {
            timerText.text = "0:00"
            onFinish()
        }
    }.start()
}