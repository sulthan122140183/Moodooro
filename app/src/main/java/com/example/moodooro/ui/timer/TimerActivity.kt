package com.example.moodooro.ui.timer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.moodooro.R

class TimerActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button
    private var countDownTimer: CountDownTimer? = null
    private var timerRunning = false
    private var timeLeftInMillis: Long = START_TIME_IN_MILLIS

    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        private const val START_TIME_IN_MILLIS: Long = 1500000 // 25 minutes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerTextView = findViewById(R.id.textViewTimer)
        startPauseButton = findViewById(R.id.buttonStartPause)
        resetButton = findViewById(R.id.buttonReset)

        mediaPlayer = MediaPlayer.create(this, R.raw.timer_finish_sound)

        startPauseButton.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }

        updateCountDownText()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timerRunning = false
                updateButtons()
                mediaPlayer.start()
            }
        }.start()

        timerRunning = true
        updateButtons()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        updateButtons()
    }

    private fun resetTimer() {
        timeLeftInMillis = START_TIME_IN_MILLIS
        updateCountDownText()
        updateButtons()
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = timeFormatted
    }

    private fun updateButtons() {
        if (timerRunning) {
            startPauseButton.text = getString(R.string.pause)
            resetButton.visibility = View.INVISIBLE
        } else {
            startPauseButton.text = getString(R.string.start)
            resetButton.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
