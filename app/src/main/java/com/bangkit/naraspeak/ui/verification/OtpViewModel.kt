package com.bangkit.naraspeak.ui.verification

import android.os.CountDownTimer
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Timer
import java.util.TimerTask

class OtpViewModel: ViewModel() {

    private val _remainingTime = MutableLiveData<Long>()
    val remainingTime: LiveData<Long> get() = _remainingTime

    private val _isResetTimer = MutableLiveData<Boolean>()
    val isResetTimer: LiveData<Boolean> get() = _isResetTimer


    init {
        timer()


    }

    fun timer() {
        _isResetTimer.value = true
        _remainingTime.value = ONE_MINUTE
        object : CountDownTimer(ONE_SECOND, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = ONE_MINUTE - millisUntilFinished
            }

            override fun onFinish() {
                _remainingTime.value = 0
            }

        }
    }


    companion object {
        const val ONE_SECOND = 1000L
        const val ONE_MINUTE = ONE_SECOND * 60
    }
}