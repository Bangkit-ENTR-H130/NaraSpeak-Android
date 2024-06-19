package com.bangkit.naraspeak.ui.verification

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Timer
import java.util.TimerTask

class OtpViewModel: ViewModel() {
    companion object {
        private const val ONE_SEC = 1000
        private const val INITIAL_TIME = 60 // initial time in seconds
    }

    private val mElapsedTime = MutableLiveData<Long?>()
    private var timer: Timer? = null

    init {
        resetTimer()
    }

    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentValue = mElapsedTime.value ?: INITIAL_TIME.toLong()
                if (currentValue > 0) {
                    val newValue = currentValue - 1
                    mElapsedTime.postValue(newValue)
                } else {
                    timer?.cancel()
                }
            }
        }, ONE_SEC.toLong(), ONE_SEC.toLong())
    }

    fun resetTimer() {
        mElapsedTime.postValue(INITIAL_TIME.toLong())
        timer?.cancel()
        startTimer()
    }

    fun getElapsedTime(): LiveData<Long?> {
        return mElapsedTime
    }
}
