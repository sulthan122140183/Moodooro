package com.example.moodooro.ui.mood // Pastikan package ini sesuai

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Menggunakan AlertDialog dari AppCompat untuk konsistensi
import androidx.fragment.app.DialogFragment
import com.example.moodooro.R
import com.example.moodooro.databinding.FragmentMoodInputDialogBinding
import com.example.moodooro.ui.timer.TimerActivity // Untuk memanggil fungsi callback

class MoodInputDialogFragment : DialogFragment() {

    private var _binding: FragmentMoodInputDialogBinding? = null
    // Properti ini hanya valid antara onCreateView dan onDestroyView.
    private val binding get() = _binding!!

    companion object {
        const val TAG = "MoodInputDialogFragment" // TAG untuk menampilkan dialog
    }

    // Tidak menggunakan onCreateView standar untuk DialogFragment jika kita meng-override onCreateDialog
    // Namun, jika kita ingin layout kustom penuh tanpa tombol default dialog, onCreateView bisa digunakan.
    // Di sini kita akan menggunakan onCreateDialog untuk memanfaatkan builder AlertDialog.

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentMoodInputDialogBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity(), R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
            .setView(binding.root) // Menetapkan layout kustom kita ke dialog
        // Kita tidak akan menambahkan tombol Positif/Negatif bawaan AlertDialog di sini,
        // karena kita sudah punya tombol "Simpan" di layout kustom kita.

        setupDialogViewListeners()

        val dialog = builder.create()
        // Mencegah dialog ditutup saat menyentuh area di luar dialog
        dialog.setCanceledOnTouchOutside(false)
        // Mencegah dialog ditutup saat tombol back ditekan (opsional, bisa dipertimbangkan)
        // dialog.setCancelable(false)

        return dialog
    }

    private fun setupDialogViewListeners() {
        binding.buttonSaveMood.setOnClickListener {
            val selectedMoodId = binding.radioGroupMood.checkedRadioButtonId
            if (selectedMoodId == -1) {
                Toast.makeText(context, getString(R.string.toast_select_mood_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mood = when (selectedMoodId) {
                binding.radioButtonHappy.id -> getString(R.string.mood_happy)
                binding.radioButtonNeutral.id -> getString(R.string.mood_neutral)
                binding.radioButtonNotFocused.id -> getString(R.string.mood_not_focused)
                else -> "Unknown" // Seharusnya tidak terjadi
            }

            // Di sini Anda akan menyimpan mood ini.
            // Untuk sekarang, kita tampilkan Toast dan beri tahu TimerActivity.
            // Nantinya, ini bisa memanggil fungsi di ViewModel yang terhubung ke Repository/Database.
            Toast.makeText(context, getString(R.string.toast_mood_saved, mood), Toast.LENGTH_LONG).show()

            // Memanggil fungsi di TimerActivity untuk memberi tahu bahwa input mood selesai
            // dan ViewModel bisa melanjutkan ke sesi berikutnya.
            (activity as? TimerActivity)?.onMoodDialogActionCompleted()

            dismiss() // Menutup dialog
        }
    }

    // Penting untuk membersihkan binding agar tidak terjadi memory leak
    override fun onDestroyView() {
        // Jika kita meng-inflate view di onCreateView, _binding harus di-null di sini.
        // Karena kita menggunakan setView di onCreateDialog, DialogFragment menangani siklus hidup view sedikit berbeda.
        // Namun, membersihkan _binding tetap praktik yang baik jika Anda mungkin mengubah cara view di-inflate.
        // Jika view hanya digunakan dalam lingkup onCreateDialog, _binding mungkin tidak perlu global.
        // Tapi untuk akses di setupDialogViewListeners, kita membuatnya properti kelas.
        super.onDestroyView() // Penting untuk memanggil super
        _binding = null
    }

    // Anda bisa menghapus onCreateView dan onViewCreated jika menggunakan onCreateDialog dengan setView.
    // Jika Anda memutuskan untuk tidak menggunakan AlertDialog.Builder dan ingin kustomisasi penuh:
    /*
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodInputDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent) // Untuk dialog dengan sudut rounded
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialogViewListeners()
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onStart() {
        super.onStart()
        // Mengatur lebar dialog jika menggunakan onCreateView untuk layout penuh
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        // Anda mungkin perlu mengatur margin horizontal di root layout XML dialog Anda.
    }
    */
}