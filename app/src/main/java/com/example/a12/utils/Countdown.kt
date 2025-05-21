package com.example.a12.utils.countdown

import android.os.CountDownTimer
import android.widget.TextView
import com.example.a12.model.DAO.TestDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun startCountdown(
    initialMillis: Long,
    timerText: TextView,
    resultId: Long,
    testDao: TestDao,
    scope: CoroutineScope,
    onFinish: () -> Unit
): CountDownTimer {
    return object : CountDownTimer(initialMillis, 1_000) {
        override fun onTick(millisUntilFinished: Long) {
            val m = millisUntilFinished / 60_000
            val s = (millisUntilFinished % 60_000) / 1_000
            timerText.text = String.format("%d:%02d", m, s)
            val secondsLeft = (millisUntilFinished / 1_000).toInt()
            scope.launch {
                testDao.updateRemainingSeconds(resultId, secondsLeft)
            }
        }

        override fun onFinish() {
            timerText.text = "0:00"
            onFinish()
        }
    }.start()

    suspend fun getInitialMillis(resultId: Long, testDao: TestDao): Long {
        val remainingSeconds = testDao.getRemainingSeconds(resultId)
        if (remainingSeconds != null && remainingSeconds > 0) {
            return remainingSeconds * 1000L
        }
        val testId = testDao.getTestIdByResult(resultId)
        val minutes = testDao.getTestDurationMinutes(testId)
        return minutes * 60_000L
    }
}