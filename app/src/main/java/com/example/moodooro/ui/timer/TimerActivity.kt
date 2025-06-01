package com.example.moodooro.ui.timer

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.moodooro.R
import com.example.moodooro.data.local.MoodooroDatabase
import com.example.moodooro.data.local.entity.StudySessionEntity
import com.example.moodooro.data.repository.StudySessionRepository
import com.example.moodooro.databinding.ActivityTimerBinding
import com.example.moodooro.ui.main.TIMER_DURATION_MINUTES
import com.example.moodooro.viewModels.StudySessionViewModel
import com.example.moodooro.viewModels.StudySessionViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private lateinit var timerTextView: TextView
    private lateinit var buttonStartPause: MaterialButton
    private lateinit var buttonReset: MaterialButton
    private lateinit var buttonBack: ImageButton
    private lateinit var layoutFocusOptions: LinearLayout
    private lateinit var buttonSaveFocused: MaterialButton
    private lateinit var buttonSaveDistracted: MaterialButton
    private lateinit var buttonRecordMood: MaterialButton
    private lateinit var buttonSkipMood: MaterialButton
    private lateinit var buttonMoodGood: MaterialButton
    private lateinit var buttonMoodBad: MaterialButton
    private lateinit var buttonStartBreak: MaterialButton // This line was already here

    private var countDownTimer: CountDownTimer? = null
    private var timerRunning: Boolean = false
    private var timerFinished: Boolean = false
    private var timeLeftInMillis: Long = 0
    private var initialTimerDurationMillis: Long = 0L
    private var currentSessionId: Long = -1L
    private var currentSessionOutcome: String? = null
    private var currentSessionSubject: String? = null
    private var currentSessionTimestamp: Long = 0L

    private var focusSessionSaved: Boolean = false
    private var moodSelectionStepActive: Boolean = false
    private var moodRecordedOrSkipped: Boolean = false
    private var isBreakModeActive: Boolean = false
    private var originalStudyDurationMillis: Long = 0L


    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val DATABASE_ID_KEY = "database_id_key"
    }

    private val viewModel: StudySessionViewModel by viewModels {
        StudySessionViewModelFactory(
            StudySessionRepository(
                runBlocking { MoodooroDatabase.getSuspendingDatabase(applicationContext).studySessionDao() }
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timerTextView = binding.timerTextView
        buttonStartPause = binding.buttonStartPause
        buttonReset = binding.buttonReset
        buttonBack = binding.buttonBack
        layoutFocusOptions = binding.layoutFocusOptions
        buttonSaveFocused = binding.buttonSaveFocused
        buttonSaveDistracted = binding.buttonSaveDistracted
        buttonRecordMood = binding.buttonRecordMood
        buttonSkipMood = binding.buttonSkipMood
        buttonMoodGood = binding.buttonMoodGood
        buttonMoodBad = binding.buttonMoodBad
        // Make sure the ID button_start_break is in your activity_timer.xml
        buttonStartBreak = binding.buttonStartBreak


        mediaPlayer = MediaPlayer.create(this, R.raw.timer_finish_sound)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val receivedDurationMinutes = intent.getIntExtra(TIMER_DURATION_MINUTES, 25)
        initialTimerDurationMillis = if (receivedDurationMinutes == 0) {
            5000L // 5 seconds for demo
        } else {
            TimeUnit.MINUTES.toMillis(receivedDurationMinutes.toLong())
        }
        originalStudyDurationMillis = initialTimerDurationMillis
        timeLeftInMillis = initialTimerDurationMillis
        currentSessionSubject = intent.getStringExtra("STUDY_SESSION_SUBJECT") ?: getDefaultSubjectName()

        buttonBack.setOnClickListener {
            finish()
        }

        buttonStartPause.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        buttonReset.setOnClickListener {
            if (isBreakModeActive){
                isBreakModeActive = false
                Toast.makeText(this@TimerActivity, "Istirahat dihentikan.", Toast.LENGTH_SHORT).show()
                resetTimer(resetToOriginal = true)
            } else {
                resetTimer(resetToOriginal = true)
            }
        }

        buttonSaveFocused.setOnClickListener { handleSessionSave("Focused") }
        buttonSaveDistracted.setOnClickListener { handleSessionSave("Distracted") }
        buttonRecordMood.setOnClickListener { showMoodSelectionButtons() }
        buttonSkipMood.setOnClickListener { skipMoodRecording() }
        buttonMoodGood.setOnClickListener { recordMood("Bagus") }
        buttonMoodBad.setOnClickListener { recordMood("Tidak Baik") }
        buttonStartBreak.setOnClickListener { startBreak() }

        // Observe the insertedSessionId LiveData
        viewModel.insertedSessionId.observe(this) { generatedId ->
            if (generatedId != null && generatedId != -1L) {
                currentSessionId = generatedId
                val outcomeForToast = currentSessionOutcome ?: "Sesi" // Use a fallback
                val durationForToast = TimeUnit.MILLISECONDS.toMinutes(originalStudyDurationMillis).toInt() // Or actualDurationMillis
                Log.d("TimerActivity", "Sesi disimpan dengan ID: $generatedId, Outcome: $outcomeForToast, Duration: $durationForToast min")
                Toast.makeText(this, "$outcomeForToast disimpan", Toast.LENGTH_SHORT).show()
                viewModel.clearLastInsertedSessionId() // Clear to prevent re-triggering
            }
        }

        updateCountDownText()
        updateButtons()
    }

    private fun getDefaultSubjectName(): String {
        val sdf = SimpleDateFormat("EEE, MMM d, HH:mm", Locale.getDefault())
        return "Sesi Belajar - ${sdf.format(Date())}"
    }

    private fun startTimer() {
        if (timerRunning) return

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timerRunning = false
                timerFinished = true
                mediaPlayer?.start()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(500)
                }

                if (isBreakModeActive) {
                    isBreakModeActive = false
                    Toast.makeText(this@TimerActivity, "Waktu istirahat selesai! Siap untuk sesi baru.", Toast.LENGTH_LONG).show()
                    resetTimerAndSessionState(resetToOriginalDuration = true)
                } else {
                    currentSessionTimestamp = System.currentTimeMillis()
                    focusSessionSaved = false
                    moodSelectionStepActive = false
                    moodRecordedOrSkipped = false
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

    private fun resetTimer(resetToOriginal: Boolean = true) {
        pauseTimer()
        resetTimerAndSessionState(resetToOriginalDuration = resetToOriginal)
        updateButtons()
    }

    private fun resetTimerAndSessionState(resetToOriginalDuration: Boolean) {
        countDownTimer?.cancel()
        timerRunning = false
        timerFinished = false
        focusSessionSaved = false
        moodSelectionStepActive = false
        moodRecordedOrSkipped = false

        if (resetToOriginalDuration) {
            timeLeftInMillis = originalStudyDurationMillis
            isBreakModeActive = false
        }
        updateCountDownText()
    }

    private fun updateButtons() {
        layoutFocusOptions.visibility = View.GONE
        buttonSaveFocused.visibility = View.GONE
        buttonSaveDistracted.visibility = View.GONE
        buttonRecordMood.visibility = View.GONE
        buttonSkipMood.visibility = View.GONE
        buttonMoodGood.visibility = View.GONE
        buttonMoodBad.visibility = View.GONE
        buttonStartBreak.visibility = View.GONE

        if (isBreakModeActive) {
            buttonStartPause.text = if (timerRunning) getString(R.string.pause) else "Lanjutkan" // Ensure R.string.resume is "Lanjutkan" or use hardcoded
            buttonStartPause.visibility = View.VISIBLE
            buttonReset.visibility = View.VISIBLE
            buttonReset.text = "Hentikan Istirahat"
        } else {
            buttonReset.text = getString(R.string.reset)
            if (timerRunning) {
                buttonStartPause.text = getString(R.string.pause)
                buttonStartPause.visibility = View.VISIBLE
                buttonReset.visibility = View.VISIBLE
            } else {
                if (timerFinished) {
                    if (!focusSessionSaved) {
                        layoutFocusOptions.visibility = View.VISIBLE
                        buttonSaveFocused.visibility = View.VISIBLE
                        buttonSaveDistracted.visibility = View.VISIBLE
                        buttonStartPause.visibility = View.GONE
                        buttonReset.visibility = View.GONE
                    } else if (!moodRecordedOrSkipped) {
                        layoutFocusOptions.visibility = View.VISIBLE
                        if (moodSelectionStepActive) {
                             buttonMoodGood.visibility = View.VISIBLE
                             buttonMoodBad.visibility = View.VISIBLE
                        } else {
                            buttonRecordMood.visibility = View.VISIBLE
                            buttonSkipMood.visibility = View.VISIBLE
                        }
                        buttonStartPause.visibility = View.GONE
                        buttonReset.visibility = View.GONE
                    } else { // focusSessionSaved and moodRecordedOrSkipped
                        layoutFocusOptions.visibility = View.VISIBLE
                        buttonStartBreak.visibility = View.VISIBLE // Show start break button
                        buttonStartPause.visibility = View.GONE
                        buttonReset.visibility = View.GONE
                    }
                } else { // Timer not finished, not running (i.e. initial state or after reset)
                    buttonStartPause.text = getString(R.string.start)
                    buttonStartPause.visibility = View.VISIBLE
                    buttonReset.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun handleSessionSave(outcome: String) {
        currentSessionOutcome = outcome // Store outcome for the observer
        focusSessionSaved = true
        moodSelectionStepActive = false
        moodRecordedOrSkipped = false // Reset this as mood recording is the next step

        val actualDurationMillis = if (timerFinished) originalStudyDurationMillis else originalStudyDurationMillis - timeLeftInMillis
        val actualDurationMinutes = TimeUnit.MILLISECONDS.toMinutes(actualDurationMillis).toInt()

        val endTimeMillis = currentSessionTimestamp // Set in onFinish() if timer completed
        val startTimeMillis = endTimeMillis - actualDurationMillis

        val calendar = Calendar.getInstance().apply {
            timeInMillis = endTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateMillis = calendar.timeInMillis

        val session = StudySessionEntity(
            id = 0L,
            timestamp = endTimeMillis,
            durationMillis = actualDurationMillis,
            focusStatus = outcome,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            focusDurationMinutes = actualDurationMinutes,
            breakDurationMinutes = 0,
            subject = currentSessionSubject ?: "Sesi Belajar",
            sessionOutcome = outcome,
            dateMillis = dateMillis
            // Mood will be updated via updateMoodForSession
        )

        viewModel.insertStudySession(session) // This will trigger the LiveData observer in onCreate

        updateButtons() // Update UI to show mood options
    }

    private fun showMoodSelectionButtons() {
        moodSelectionStepActive = true
        updateButtons()
    }

    private fun recordMood(moodValue: String) {
        if (currentSessionId != -1L) {
            viewModel.updateMoodForSession(currentSessionId, moodValue)
            Log.d("TimerActivity", "Mood '$moodValue' direkam untuk sesi ID $currentSessionId")
            Toast.makeText(this, "Mood '$moodValue' dicatat", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("TimerActivity", "Gagal merekam mood, session ID tidak valid atau belum diset dari observer.")
            Toast.makeText(this, "Gagal mencatat mood (ID Sesi belum ada)", Toast.LENGTH_SHORT).show()
        }
        moodRecordedOrSkipped = true
        moodSelectionStepActive = false
        updateButtons()
    }

    private fun skipMoodRecording() {
        Log.d("TimerActivity", "Perekaman mood dilewati untuk sesi ID $currentSessionId")
        Toast.makeText(this, "Pencatatan mood dilewati", Toast.LENGTH_SHORT).show()
        moodRecordedOrSkipped = true
        moodSelectionStepActive = false
        updateButtons()
    }

    private fun startBreak() {
        isBreakModeActive = true
        focusSessionSaved = false // Reset for the new state
        moodRecordedOrSkipped = false
        moodSelectionStepActive = false
        timerFinished = false // Break timer hasn't finished

        timeLeftInMillis = TimeUnit.MINUTES.toMillis(5L) // 5-minute break
        updateCountDownText()
        startTimer() // This will also call updateButtons()
    }

    private fun updateCountDownText() {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) - TimeUnit.MINUTES.toSeconds(minutes)
        timerTextView.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
