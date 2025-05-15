package com.example.moodooro.ui.timer // Pastikan package ini sesuai dengan struktur folder Anda

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.TimeUnit

class TimerViewModel : ViewModel() {

    companion object {
        // Durasi dalam milidetik (sesuai proposal: 25 menit fokus, 5 menit istirahat)
        private val FOCUS_DURATION_MS = TimeUnit.MINUTES.toMillis(25)
        private val SHORT_BREAK_DURATION_MS = TimeUnit.MINUTES.toMillis(5)
        private val LONG_BREAK_DURATION_MS = TimeUnit.MINUTES.toMillis(15) // Contoh istirahat panjang
        private const val SESSIONS_BEFORE_LONG_BREAK = 4 // Jumlah sesi fokus sebelum istirahat panjang
    }

    // Enum untuk merepresentasikan state dari timer
    enum class TimerState {
        IDLE,       // Timer belum dimulai atau sudah direset
        RUNNING,    // Timer sedang berjalan
        PAUSED,     // Timer dijeda
        FINISHED    // Satu sesi timer telah selesai
    }

    // Enum untuk merepresentasikan tipe sesi saat ini
    enum class SessionType {
        FOCUS,
        SHORT_BREAK,
        LONG_BREAK
    }

    private var countDownTimer: CountDownTimer? = null

    // LiveData untuk waktu saat ini dalam milidetik
    private val _currentTimeMillis = MutableLiveData<Long>()
    val currentTimeMillis: LiveData<Long> get() = _currentTimeMillis

    // LiveData untuk waktu saat ini dalam format string (MM:SS) untuk ditampilkan di UI
    private val _currentTimeString = MutableLiveData<String>()
    val currentTimeString: LiveData<String> get() = _currentTimeString

    // LiveData untuk state timer saat ini
    private val _timerState = MutableLiveData(TimerState.IDLE)
    val timerState: LiveData<TimerState> get() = _timerState

    // LiveData untuk tipe sesi saat ini
    private val _currentSessionType = MutableLiveData(SessionType.FOCUS)
    val currentSessionType: LiveData<SessionType> get() = _currentSessionType

    // LiveData untuk menghitung jumlah sesi fokus yang telah selesai dalam satu siklus
    private val _focusSessionsCompletedThisCycle = MutableLiveData(0)
    val focusSessionsCompletedThisCycle: LiveData<Int> get() = _focusSessionsCompletedThisCycle

    // LiveData sebagai event untuk memberi tahu UI agar menampilkan tombol/dialog input mood
    private val _showMoodInputEvent = MutableLiveData<Boolean>()
    val showMoodInputEvent: LiveData<Boolean> get() = _showMoodInputEvent

    // Variabel internal
    private var timeRemainingInMillis: Long = FOCUS_DURATION_MS
    private var playSoundOnSessionFinish: Boolean = false

    init {
        // Inisialisasi timer saat ViewModel dibuat
        setInitialTimeForCurrentSession()
    }

    private fun setInitialTimeForCurrentSession() {
        timeRemainingInMillis = when (_currentSessionType.value) {
            SessionType.FOCUS -> FOCUS_DURATION_MS
            SessionType.SHORT_BREAK -> SHORT_BREAK_DURATION_MS
            SessionType.LONG_BREAK -> LONG_BREAK_DURATION_MS
            else -> FOCUS_DURATION_MS // Default
        }
        _currentTimeMillis.value = timeRemainingInMillis
        _currentTimeString.value = formatTime(timeRemainingInMillis)
        _timerState.value = TimerState.IDLE
    }

    fun toggleTimer() {
        when (_timerState.value) {
            TimerState.IDLE, TimerState.PAUSED, TimerState.FINISHED -> startTimer()
            TimerState.RUNNING -> pauseTimer()
            null -> { // Seharusnya tidak terjadi jika init benar, tapi sebagai pengaman
                setInitialTimeForCurrentSession()
                startTimer()
            }
        }
    }

    private fun startTimer() {
        // Jika timer selesai dari sesi sebelumnya dan ingin memulai sesi baru,
        // pastikan waktu direset ke durasi sesi saat ini.
        if (_timerState.value == TimerState.FINISHED) {
            setInitialTimeForCurrentSession() // Ini akan mengatur ulang state ke IDLE juga
        }

        _timerState.value = TimerState.RUNNING
        countDownTimer?.cancel() // Selalu batalkan timer sebelumnya jika ada untuk menghindari duplikasi
        countDownTimer = object : CountDownTimer(timeRemainingInMillis, 1000) { // Tick setiap 1 detik
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                _currentTimeMillis.value = timeRemainingInMillis
                _currentTimeString.value = formatTime(timeRemainingInMillis)
            }

            override fun onFinish() {
                handleSessionFinish()
            }
        }.start()
    }

    private fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            _timerState.value = TimerState.PAUSED
            countDownTimer?.cancel()
        }
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        // Saat reset, kembali ke sesi fokus awal dan reset hitungan sesi
        _currentSessionType.value = SessionType.FOCUS
        _focusSessionsCompletedThisCycle.value = 0
        setInitialTimeForCurrentSession() // Ini akan mengatur state ke IDLE dan waktu yang benar
        _showMoodInputEvent.value = false // Sembunyikan tombol mood jika direset
    }

    private fun handleSessionFinish() {
        playSoundOnSessionFinish = true // Tandai untuk memutar suara di Activity
        _timerState.value = TimerState.FINISHED

        if (_currentSessionType.value == SessionType.FOCUS) {
            val currentCompleted = _focusSessionsCompletedThisCycle.value ?: 0
            _focusSessionsCompletedThisCycle.value = currentCompleted + 1
            _showMoodInputEvent.value = true // Tampilkan input mood setelah sesi FOKUS selesai
        } else {
            // Jika sesi ISTIRAHAT selesai, langsung lanjutkan ke sesi berikutnya
            // tanpa perlu input mood.
            proceedToNextSessionLogic()
        }
    }

    /**
     * Dipanggil dari Activity setelah pengguna selesai mencatat mood.
     */
    fun userFinishedMoodInput() {
        _showMoodInputEvent.value = false // Reset event setelah digunakan
        proceedToNextSessionLogic()
    }

    /**
     * Logika inti untuk menentukan dan memulai sesi berikutnya.
     */
    private fun proceedToNextSessionLogic() {
        val currentSession = _currentSessionType.value
        val completedInCycle = _focusSessionsCompletedThisCycle.value ?: 0

        when (currentSession) {
            SessionType.FOCUS -> {
                // Setelah sesi fokus, tentukan apakah istirahat pendek atau panjang
                _currentSessionType.value = if (completedInCycle > 0 && completedInCycle % SESSIONS_BEFORE_LONG_BREAK == 0) {
                    SessionType.LONG_BREAK
                } else {
                    SessionType.SHORT_BREAK
                }
            }
            SessionType.SHORT_BREAK -> {
                // Setelah istirahat pendek, kembali ke fokus
                _currentSessionType.value = SessionType.FOCUS
            }
            SessionType.LONG_BREAK -> {
                // Setelah istirahat panjang, kembali ke fokus dan reset hitungan siklus
                _currentSessionType.value = SessionType.FOCUS
                _focusSessionsCompletedThisCycle.value = 0 // Reset siklus
            }
            else -> _currentSessionType.value = SessionType.FOCUS // Default jika null (seharusnya tidak)
        }
        // Atur ulang timer untuk sesi baru yang telah ditentukan
        // Ini akan otomatis mengatur state ke IDLE dan waktu yang benar
        setInitialTimeForCurrentSession()
        // Timer tidak dimulai otomatis, pengguna harus menekan "Mulai" lagi untuk sesi berikutnya.
    }

    /**
     * Memformat waktu dari milidetik menjadi string "MM:SS".
     */
    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Digunakan oleh Activity untuk mengecek apakah suara perlu diputar.
     * Flag akan direset setelah dicek.
     */
    fun shouldPlaySoundOnFinish(): Boolean {
        val playSound = playSoundOnSessionFinish
        if (playSound) {
            playSoundOnSessionFinish = false // Reset flag setelah dicek
        }
        return playSound
    }

    /**
     * Membersihkan resource (seperti CountDownTimer) saat ViewModel tidak lagi digunakan.
     */
    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel() // Penting untuk mencegah memory leak
    }
}