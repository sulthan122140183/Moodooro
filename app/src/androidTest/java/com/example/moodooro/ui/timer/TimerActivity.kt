package com.example.moodooro.ui.timer // Pastikan package ini sesuai

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.moodooro.R // Pastikan R diimpor dengan benar
import com.example.moodooro.databinding.ActivityTimerBinding
import com.example.moodooro.ui.mood.MoodInputDialogFragment // Impor untuk dialog mood

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    // Menggunakan 'by viewModels()' dari activity-ktx untuk mendapatkan instance ViewModel
    private val timerViewModel: TimerViewModel by viewModels()

    private var soundPool: SoundPool? = null
    private var timerFinishSoundId: Int = 0
    private var soundLoaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menggunakan ViewBinding untuk mengakses elemen UI dengan aman dan efisien
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSoundPool()
        setupObservers()
        setupClickListeners()

        // Memperbarui tampilan tombol saat activity pertama kali dibuat
        // berdasarkan state awal dari ViewModel
        timerViewModel.timerState.value?.let { updateButtonStatesAndIcons(it) }
        timerViewModel.currentSessionType.value?.let { updateSessionStatusText(it) }
    }

    private fun setupSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM) // Lebih sesuai untuk suara notifikasi/alarm timer
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1) // Cukup satu stream untuk notifikasi timer
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == timerFinishSoundId) {
                soundLoaded = true // Suara berhasil dimuat dan siap dimainkan
            }
            // Anda bisa menambahkan log atau toast di sini jika ada error saat load (status != 0)
        }
        // Muat suara dari res/raw.
        // Pastikan nama file 'timer_finish_sound' sama dengan nama file .wav Anda di res/raw (tanpa ekstensi).
        timerFinishSoundId = soundPool?.load(this, R.raw.timer_finish_sound, 1) ?: 0
        if (timerFinishSoundId == 0) {
            // Gagal mendapatkan ID suara, bisa jadi file tidak ditemukan atau masalah lain.
            // Tambahkan log atau pemberitahuan jika perlu.
            // Log.e("TimerActivity", "Gagal memuat suara timer_finish_sound.wav")
        }
    }

    private fun playTimerFinishSound() {
        if (soundLoaded && timerFinishSoundId != 0) {
            soundPool?.play(timerFinishSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    private fun setupObservers() {
        // Mengamati perubahan pada currentTimeString dari ViewModel
        timerViewModel.currentTimeString.observe(this) { timeString ->
            animateTextUpdate(binding.textViewTimer, timeString)
        }

        // Mengamati perubahan pada timerState dari ViewModel
        timerViewModel.timerState.observe(this) { state ->
            updateButtonStatesAndIcons(state) // Perbarui tampilan tombol berdasarkan state
            if (timerViewModel.shouldPlaySoundOnFinish()) { // Cek flag dari ViewModel
                playTimerFinishSound()
            }
        }

        // Mengamati perubahan pada currentSessionType dari ViewModel
        timerViewModel.currentSessionType.observe(this) { sessionType ->
            updateSessionStatusText(sessionType)
        }

        // Mengamati event untuk menampilkan dialog input mood
        timerViewModel.showMoodInputEvent.observe(this) { show ->
            if (show) {
                animateViewVisibility(binding.buttonFinishSession, true) // Tampilkan tombol catat mood
                // ViewModel akan menangani reset event ini setelah digunakan
            }
            // Tombol akan disembunyikan otomatis oleh updateButtonStates atau saat reset
        }

        // (Opsional) Mengamati jumlah sesi fokus yang selesai dalam satu siklus
        timerViewModel.focusSessionsCompletedThisCycle.observe(this) { count ->
            // Implementasi untuk menampilkan textViewSessionCount jika Anda menambahkannya di layout
            binding.textViewSessionCount?.text = "Siklus Selesai: $count / ${TimerViewModel.SESSIONS_BEFORE_LONG_BREAK}"
        }
    }

    private fun updateSessionStatusText(sessionType: TimerViewModel.SessionType?) {
        val statusTextResId = when (sessionType) {
            TimerViewModel.SessionType.FOCUS -> R.string.session_status_focus
            TimerViewModel.SessionType.SHORT_BREAK -> R.string.session_status_short_break
            TimerViewModel.SessionType.LONG_BREAK -> R.string.session_status_long_break
            else -> R.string.session_status_focus // Default
        }
        animateTextUpdate(binding.textViewSessionStatus, getString(statusTextResId))
    }


    private fun updateButtonStatesAndIcons(state: TimerViewModel.TimerState?) {
        when (state) {
            TimerViewModel.TimerState.IDLE -> {
                binding.buttonStartPause.text = getString(R.string.button_start)
                binding.buttonStartPause.icon = ContextCompat.getDrawable(this, R.drawable.ic_play_arrow)
                binding.buttonStartPause.isEnabled = true
                binding.buttonReset.isEnabled = false // Reset tidak aktif jika belum pernah dimulai
                animateViewVisibility(binding.buttonFinishSession, false, 0) // Sembunyikan tombol mood
            }
            TimerViewModel.TimerState.RUNNING -> {
                binding.buttonStartPause.text = getString(R.string.button_pause)
                binding.buttonStartPause.icon = ContextCompat.getDrawable(this, R.drawable.ic_pause)
                binding.buttonStartPause.isEnabled = true
                binding.buttonReset.isEnabled = true // Bisa reset saat running
                animateViewVisibility(binding.buttonFinishSession, false, 0)
            }
            TimerViewModel.TimerState.PAUSED -> {
                binding.buttonStartPause.text = getString(R.string.button_resume)
                binding.buttonStartPause.icon = ContextCompat.getDrawable(this, R.drawable.ic_play_arrow)
                binding.buttonStartPause.isEnabled = true
                binding.buttonReset.isEnabled = true
                animateViewVisibility(binding.buttonFinishSession, false, 0)
            }
            TimerViewModel.TimerState.FINISHED -> {
                binding.buttonStartPause.text = getString(R.string.button_start) // Siap untuk sesi berikutnya
                binding.buttonStartPause.icon = ContextCompat.getDrawable(this, R.drawable.ic_play_arrow)
                binding.buttonStartPause.isEnabled = true // Bisa memulai sesi berikutnya
                binding.buttonReset.isEnabled = true
                // Visibilitas tombol mood dikontrol oleh showMoodInputEvent observer
                // Jika tidak ada input mood (misal setelah istirahat), tombol start langsung bisa ditekan.
            }
            else -> {
                // State default jika null atau tidak diketahui
                binding.buttonStartPause.text = getString(R.string.button_start)
                binding.buttonStartPause.icon = ContextCompat.getDrawable(this, R.drawable.ic_play_arrow)
                binding.buttonStartPause.isEnabled = true
                binding.buttonReset.isEnabled = false
                animateViewVisibility(binding.buttonFinishSession, false, 0)
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonStartPause.setOnClickListener {
            timerViewModel.toggleTimer()
        }

        binding.buttonReset.setOnClickListener {
            timerViewModel.resetTimer()
            // Pastikan tombol mood juga disembunyikan saat reset manual
            // Visibilitas tombol mood akan diatur oleh observer _showMoodInputEvent yang akan menjadi false
        }

        binding.buttonFinishSession.setOnClickListener {
            // Tampilkan dialog untuk input mood
            val dialog = MoodInputDialogFragment()
            dialog.show(supportFragmentManager, MoodInputDialogFragment.TAG)
            // Sembunyikan tombol setelah diklik, ViewModel akan mengatur logika sesi berikutnya
            animateViewVisibility(binding.buttonFinishSession, false)
        }
    }

    // Fungsi untuk animasi sederhana pada pembaruan teks
    private fun animateTextUpdate(textView: TextView, newText: String) {
        if (textView.text.toString() == newText && textView.alpha == 1f) return // Hindari animasi jika teks & alpha sama

        textView.animate()
            .alpha(0f)
            .setDuration(150) // Durasi fade out
            .withEndAction {
                textView.text = newText
                textView.animate()
                    .alpha(1f)
                    .setDuration(150) // Durasi fade in
                    .start()
            }
            .start()
    }

    // Fungsi untuk animasi sederhana pada visibilitas view
    private fun animateViewVisibility(view: View, show: Boolean, duration: Long = 300) {
        val targetAlpha = if (show) 1f else 0f
        // Hindari animasi jika state sudah sesuai
        if (view.visibility == (if (show) View.VISIBLE else View.GONE) && view.alpha == targetAlpha) return

        if (show) {
            view.alpha = 0f // Mulai dari transparan jika akan ditampilkan
            view.visibility = View.VISIBLE
        }

        view.animate()
            .alpha(targetAlpha)
            .setDuration(duration)
            .setListener(null) // Hapus listener animasi sebelumnya
            .withEndAction {
                if (!show) {
                    view.visibility = View.GONE // Sembunyikan setelah animasi selesai jika targetnya hide
                }
            }
            .start()
    }

    /**
     * Callback dari MoodInputDialogFragment setelah pengguna menyimpan mood.
     * Memerintahkan ViewModel untuk melanjutkan ke sesi berikutnya.
     */
    fun onMoodDialogActionCompleted() {
        timerViewModel.userFinishedMoodInput()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release() // Penting untuk melepaskan resource SoundPool
        soundPool = null
    }
}