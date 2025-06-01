package com.example.moodooro.ui.timer

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moodooro.R
import com.example.moodooro.data.local.MoodoroDatabase
import com.example.moodooro.data.local.dao.StudySessionDao
import com.example.moodooro.data.local.entity.StudySessionEntity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TIMER_DURATION_MINUTES = "TIMER_DURATION_MINUTES"

class TimerActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var startPauseButton: MaterialButton
    private lateinit var resetButton: MaterialButton
    private lateinit var backButton: ImageButton

    private lateinit var focusOptionsLayout: LinearLayout
    private lateinit var saveFocusedButton: MaterialButton
    private lateinit var saveDistractedButton: MaterialButton
    private lateinit var buttonRecordMood: MaterialButton
    private lateinit var buttonSkipMood: MaterialButton
    private lateinit var buttonMoodGood: MaterialButton // Tombol baru
    private lateinit var buttonMoodBad: MaterialButton  // Tombol baru

    private var countDownTimer: CountDownTimer? = null
    private var timerRunning = false
    private var timeLeftInMillis: Long = 0
    private var initialTimerDurationMillis: Long = 0

    private var timerFinished = false
    private var focusSessionSaved = false
    private var moodSelectionStepActive = false // State baru untuk tahap pemilihan mood

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var studySessionDao: StudySessionDao

    companion object {
        private const val DEFAULT_START_TIME_MINUTES: Long = 25
        private const val DEMO_DURATION_SECONDS: Long = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TimerActivity_DIAGNOSTIC", "%%%%% KODE BARU VERSI DIAGNOSTIK BERJALAN %%%%%")
        setContentView(R.layout.activity_timer)

        timerTextView = findViewById(R.id.timer_text_view)
        startPauseButton = findViewById(R.id.button_start_pause)
        resetButton = findViewById(R.id.button_reset)
        backButton = findViewById(R.id.button_back)

        focusOptionsLayout = findViewById(R.id.layout_focus_options)
        saveFocusedButton = findViewById(R.id.button_save_focused)
        saveDistractedButton = findViewById(R.id.button_save_distracted)
        buttonRecordMood = findViewById(R.id.button_record_mood)
        buttonSkipMood = findViewById(R.id.button_skip_mood)
        buttonMoodGood = findViewById(R.id.button_mood_good) // Inisialisasi
        buttonMoodBad = findViewById(R.id.button_mood_bad)   // Inisialisasi

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.timer_finish_sound)
            if (mediaPlayer == null) {
                Log.e("TimerActivity", "MediaPlayer.create failed - R.raw.timer_finish_sound missing or invalid?")
            }
        } catch (e: Exception) {
            Log.e("TimerActivity", "Error creating MediaPlayer", e)
        }

        studySessionDao = MoodoroDatabase.getDatabase(applicationContext).studySessionDao()

        val durationMinutesIntent = intent.getIntExtra(TIMER_DURATION_MINUTES, DEFAULT_START_TIME_MINUTES.toInt())
        initialTimerDurationMillis = if (durationMinutesIntent == 0) {
            Log.d("TimerActivity", "Demo mode activated: Timer set to 5 seconds.")
            DEMO_DURATION_SECONDS * 1000
        } else {
            durationMinutesIntent.toLong() * 60 * 1000
        }
        timeLeftInMillis = initialTimerDurationMillis

        setupClickListeners()
        updateCountDownText()
        updateButtons()
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }
        startPauseButton.setOnClickListener { if (timerRunning) pauseTimer() else startTimer() }
        resetButton.setOnClickListener { resetTimerAndSessionState() }
        saveFocusedButton.setOnClickListener { handleSessionSave("Focused") }
        saveDistractedButton.setOnClickListener { handleSessionSave("Distracted") }

        buttonRecordMood.setOnClickListener {
            Log.d("TimerActivity", "Record Mood button clicked")
            moodSelectionStepActive = true // Set state untuk pemilihan mood
            updateButtons() // Perbarui UI untuk menampilkan tombol mood bagus/tidak
        }

        buttonSkipMood.setOnClickListener {
            Log.d("TimerActivity", "Skip Mood button clicked")
            Toast.makeText(this, "Pencatatan Mood dilewati", Toast.LENGTH_SHORT).show()
            resetTimerAndSessionState()
        }

        buttonMoodGood.setOnClickListener {
            Log.d("TimerActivity", "Mood Bagus button clicked")
            // Di sini Anda bisa menambahkan logika untuk menyimpan data "Mood Bagus"
            Toast.makeText(this, "Mood Bagus dipilih!", Toast.LENGTH_SHORT).show()
            resetTimerAndSessionState() // Kembali ke state awal timer
        }

        buttonMoodBad.setOnClickListener {
            Log.d("TimerActivity", "Mood Tidak Baik button clicked")
            // Di sini Anda bisa menambahkan logika untuk menyimpan data "Mood Tidak Baik"
            Toast.makeText(this, "Mood Tidak Baik dipilih.", Toast.LENGTH_SHORT).show()
            resetTimerAndSessionState() // Kembali ke state awal timer
        }
    }

    private fun handleSessionSave(focusStatus: String) {
        val sessionDurationMillis = initialTimerDurationMillis
        val sessionEndTimeMillis = System.currentTimeMillis()
        val sessionStartTimeMillis = sessionEndTimeMillis - sessionDurationMillis
        val durationMinutes = (sessionDurationMillis / 1000 / 60).toInt()

        val session = StudySessionEntity(
            startTimeMillis = sessionStartTimeMillis,
            endTimeMillis = sessionEndTimeMillis,
            focusDurationMinutes = durationMinutes,
            breakDurationMinutes = 0,
            subject = null,
            sessionOutcome = focusStatus,
            dateMillis = sessionStartTimeMillis
        )
        lifecycleScope.launch(Dispatchers.IO) {
            studySessionDao.insertSession(session)
        }
        Toast.makeText(this, "Sesi disimpan sebagai $focusStatus", Toast.LENGTH_SHORT).show()
        Log.d("TimerActivity", "Saved $focusStatus Session: Duration=${sessionDurationMillis}ms")

        focusSessionSaved = true
        updateButtons()
    }

    private fun resetTimerAndSessionState() {
        Log.d("TimerActivity", "resetTimerAndSessionState CALLED")
        focusOptionsLayout.visibility = View.GONE
        startPauseButton.visibility = View.VISIBLE
        resetButton.visibility = View.VISIBLE

        timerFinished = false
        focusSessionSaved = false
        moodSelectionStepActive = false // Reset state pemilihan mood
        timerRunning = false
        countDownTimer?.cancel()

        timeLeftInMillis = initialTimerDurationMillis
        updateCountDownText()
        updateButtons()
    }

    private fun startTimer() {
        if (timerFinished || focusSessionSaved || moodSelectionStepActive) {
            Log.d("TimerActivity", "startTimer: Resetting state before starting new timer because timerFinished=$timerFinished, focusSessionSaved=$focusSessionSaved, moodSelectionStepActive=$moodSelectionStepActive")
            timeLeftInMillis = initialTimerDurationMillis
            timerFinished = false
            focusSessionSaved = false
            moodSelectionStepActive = false
        }

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                Log.d("TimerActivity", "onFinish CALLED - timer has finished!")
                timerRunning = false
                timerFinished = true
                focusSessionSaved = false
                moodSelectionStepActive = false

                try {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            it.stop(); it.prepare()
                        }
                        it.start()
                        Log.d("TimerActivity", "MediaPlayer started successfully.")
                    } ?: Log.e("TimerActivity", "MediaPlayer is null in onFinish, cannot play sound.")
                } catch (e: Exception) {
                    Log.e("TimerActivity", "Error playing sound in onFinish", e)
                }
                updateButtons()
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

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = timeFormatted
    }

    private fun updateButtons() {
        Log.d("TimerActivity_STATE", "updateButtons: timerFinished=$timerFinished, focusSessionSaved=$focusSessionSaved, moodSelectionStepActive=$moodSelectionStepActive, timerRunning=$timerRunning")

        if (moodSelectionStepActive) {
            Log.d("TimerActivity_STATE", "UI STATE: Show mood selection (Bagus/Tidak Baik)")
            startPauseButton.visibility = View.GONE
            resetButton.visibility = View.GONE
            focusOptionsLayout.visibility = View.VISIBLE

            saveFocusedButton.visibility = View.GONE
            saveDistractedButton.visibility = View.GONE
            buttonRecordMood.visibility = View.GONE // Sembunyikan "Catat Mood"
            buttonSkipMood.visibility = View.GONE   // Sembunyikan "Lewati"

            buttonMoodGood.visibility = View.VISIBLE // Tampilkan "Mood Bagus"
            buttonMoodBad.visibility = View.VISIBLE  // Tampilkan "Mood Tidak Baik"

        } else if (focusSessionSaved) {
            Log.d("TimerActivity_STATE", "UI STATE: Show record mood options (Catat/Lewati)")
            startPauseButton.visibility = View.GONE
            resetButton.visibility = View.GONE
            focusOptionsLayout.visibility = View.VISIBLE

            saveFocusedButton.visibility = View.GONE
            saveDistractedButton.visibility = View.GONE
            buttonRecordMood.visibility = View.VISIBLE
            buttonSkipMood.visibility = View.VISIBLE

            buttonMoodGood.visibility = View.GONE // Sembunyikan "Mood Bagus"
            buttonMoodBad.visibility = View.GONE  // Sembunyikan "Mood Tidak Baik"

        } else if (timerFinished) {
            Log.d("TimerActivity_STATE", "UI STATE: Show save focus status options")
            startPauseButton.visibility = View.GONE
            resetButton.visibility = View.GONE
            focusOptionsLayout.visibility = View.VISIBLE

            saveFocusedButton.visibility = View.VISIBLE
            saveDistractedButton.visibility = View.VISIBLE
            buttonRecordMood.visibility = View.GONE
            buttonSkipMood.visibility = View.GONE
            buttonMoodGood.visibility = View.GONE
            buttonMoodBad.visibility = View.GONE

        } else if (timerRunning) {
            Log.d("TimerActivity_STATE", "UI STATE: Timer running")
            startPauseButton.text = getString(R.string.pause)
            startPauseButton.visibility = View.VISIBLE
            resetButton.visibility = View.INVISIBLE
            focusOptionsLayout.visibility = View.GONE

        } else {
            Log.d("TimerActivity_STATE", "UI STATE: Timer initial/paused")
            startPauseButton.text = getString(R.string.start)
            startPauseButton.visibility = View.VISIBLE
            resetButton.visibility = View.VISIBLE
            focusOptionsLayout.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.release(); Log.d("TimerActivity", "MediaPlayer released.")
        } catch (e: Exception) {
            Log.e("TimerActivity", "Error releasing MediaPlayer", e)
        }
        mediaPlayer = null
        countDownTimer?.cancel()
    }
}