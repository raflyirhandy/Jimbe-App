package com.rf.jimbe

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// PAKSA IMPORT RESOURCE PROJEK SENDIRI
import com.rf.jimbe.R

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    private var countDownTimer: CountDownTimer? = null
    private var timerRunning = false
    private var timeLeftInMillis: Long = 0

    // Deklarasi RecyclerView untuk Jadwal Latihan
    private lateinit var rvJadwalLatihan: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val cardTimer = findViewById<MaterialCardView>(R.id.cardTimer)
        val cardUpdateProgress = findViewById<MaterialCardView>(R.id.cardUpdateProgress)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)

        val ivHomeNav = findViewById<ImageView>(R.id.ivHomeNav)
        val ivStatisticNav = findViewById<ImageView>(R.id.ivStatisticNav)
        val ivChatNav = findViewById<ImageView>(R.id.ivChatNav)
        val ivProfileNav = findViewById<ImageView>(R.id.ivProfileNav)

        // Inisialisasi RecyclerView Jadwal Latihan
        rvJadwalLatihan = findViewById(R.id.rvJadwalLatihan)
        rvJadwalLatihan.layoutManager = LinearLayoutManager(this)

        tvGreeting.text = "Selamat Datang Salam Kekar,\n${currentUser.email?.substringBefore("@") ?: "Member"}!"

        // Panggil penarikan data jadwal latihan secara real-time dari Firebase
        listenJadwalLatihan(currentUser.uid)

        cardTimer.setOnClickListener {
            if (timerRunning) {
                stopTimer()
            } else {
                showSetTimerDialog()
            }
        }

        cardUpdateProgress.setOnClickListener {
            showUpdateProgressDialog(currentUser.uid)
        }

        ivHomeNav.setOnClickListener {
            Toast.makeText(this, "Anda sedang di Dashboard Utama", Toast.LENGTH_SHORT).show()
        }

        // FIX HAK AKSES STATISTIK: Kunci agar member hanya bisa membuka data statistiknya sendiri lewat UID-nya
        ivStatisticNav.setOnClickListener {
            val intent = Intent(this, StatisticActivity::class.java)
            intent.putExtra("TARGET_UID", currentUser.uid)
            startActivity(intent)
        }

        ivChatNav.setOnClickListener {
            startActivity(Intent(this, TrainerListActivity::class.java))
        }

        ivProfileNav.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    // Deklarasi RecyclerView untuk Jadwal Latihan (menggunakan KelasLatihan)
    private var listKelas = mutableListOf<KelasLatihan>()

    // Fungsi penarik data dari Firebase Realtime Database node "kelas_latihan"
    private fun listenJadwalLatihan(memberUid: String) {
        database.child("kelas_latihan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKelas.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val kelas = dataSnapshot.getValue(KelasLatihan::class.java)
                        if (kelas != null) {
                            listKelas.add(kelas)
                        }
                    }
                }

                // Set isi list ke adapter universal KelasLatihanAdapter (Member tidak bisa edit)
                rvJadwalLatihan.adapter = KelasLatihanAdapter(listKelas, isTrainer = false) { selectedKelas ->
                    // Logika Booking Kelas dengan runTransaction Firebase
                    if (selectedKelas.kuotaTersisa <= 0) {
                        Toast.makeText(this@MainActivity, "Maaf, kuota kelas ini sudah penuh.", Toast.LENGTH_SHORT).show()
                        return@KelasLatihanAdapter
                    }

                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Konfirmasi Booking")
                        .setMessage("Apakah Anda yakin ingin membooking kelas ${selectedKelas.namaLatihan}?")
                        .setPositiveButton("Ya, Booking") { _, _ ->
                            prosesBookingKelas(selectedKelas.id)
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal memuat jadwal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun prosesBookingKelas(kelasId: String) {
        val kelasRef = database.child("kelas_latihan").child(kelasId)

        val currentUserUid = auth.currentUser?.uid ?: return

        kelasRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val kelas = currentData.getValue(KelasLatihan::class.java)
                    ?: return com.google.firebase.database.Transaction.success(currentData)

                // Cek apakah member sudah terdaftar
                if (currentData.child("peserta").child(currentUserUid).value != null) {
                    return com.google.firebase.database.Transaction.abort()
                }

                if (kelas.kuotaTersisa > 0) {
                    val sisaUpdate = kelas.kuotaTersisa - 1
                    currentData.child("kuotaTersisa").value = sisaUpdate
                    // Daftarkan UID member (disimpan sementara valuenya menggunakan email fallback)
                    currentData.child("peserta").child(currentUserUid).value = auth.currentUser?.email?.substringBefore("@") ?: "Member"
                } else {
                    // Abort jika tiba-tiba penuh (race condition safety)
                    return com.google.firebase.database.Transaction.abort()
                }
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Booking Berhasil! Sisa kuota berkurang 1.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Booking Gagal: Anda sudah terdaftar atau kuota penuh.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // --- FITUR TIMER DAN DIALOG BAWAAN ASLI ANDA (TIDAK DIUBAH SAMA SEKALI) ---
    private fun showSetTimerDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Masukkan menit (1 - 120)"
            setPadding(50, 40, 50, 40)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Set Waktu Latihan")
            .setView(input)
            .setPositiveButton("Mulai") { _, _ ->
                val menit = input.text.toString().toIntOrNull() ?: 0
                if (menit in 1..120) {
                    startTimer(menit * 60 * 1000L)
                } else {
                    Toast.makeText(this, "Waktu minimal 1 menit & maksimal 120 menit", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun startTimer(durationMillis: Long) {
        timeLeftInMillis = durationMillis
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                timerRunning = false
                findViewById<TextView>(R.id.tvTimerStatus).text = "Waktu Habis!"
                playAlarmSound()
            }
        }.start()

        timerRunning = true
        findViewById<TextView>(R.id.tvTimerStatus).text = "Ketuk untuk Berhenti"
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        findViewById<TextView>(R.id.tvTimerDisplay).text = "00:00"
        findViewById<TextView>(R.id.tvTimerStatus).text = "Ketuk untuk Set"
        Toast.makeText(this, "Timer dihentikan", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        findViewById<TextView>(R.id.tvTimerDisplay).text = timeFormatted
    }

    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUpdateProgressDialog(uid: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_progress, null)
        val etTanggal = dialogView.findViewById<EditText>(R.id.etTanggal)
        val etBeban = dialogView.findViewById<EditText>(R.id.etBeban)
        val etReps = dialogView.findViewById<EditText>(R.id.etReps)
        val etDurasi = dialogView.findViewById<EditText>(R.id.etDurasi)

        var tanggalTerpilih = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etTanggal.setText(tanggalTerpilih)

        etTanggal.setOnClickListener {
            val kalender = Calendar.getInstance()
            val datePickerDialog = android.app.DatePickerDialog(this, { _, year, month, day ->
                val formatBulan = String.format("%02d", month + 1)
                val formatHari = String.format("%02d", day)
                tanggalTerpilih = "$year-$formatBulan-$formatHari"
                etTanggal.setText(tanggalTerpilih)
            }, kalender.get(Calendar.YEAR), kalender.get(Calendar.MONTH), kalender.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Input Progres Latihan")
            .setView(dialogView)
            .setPositiveButton("Simpan Data") { _, _ ->
                val beban = etBeban.text.toString().toIntOrNull() ?: 0
                val reps = etReps.text.toString().toIntOrNull() ?: 0
                val durasi = etDurasi.text.toString().toIntOrNull() ?: 0

                if (beban > 0 && reps > 0 && durasi > 0) {
                    val statData = mapOf(
                        "beban" to beban,
                        "repetisi" to reps,
                        "durasi" to durasi
                    )

                    database.child("members").child(uid).child("daily_stats").child(tanggalTerpilih)
                        .setValue(statData).addOnSuccessListener {
                            Toast.makeText(this, "Progres Latihan Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Mohon isi semua data dengan valid", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}