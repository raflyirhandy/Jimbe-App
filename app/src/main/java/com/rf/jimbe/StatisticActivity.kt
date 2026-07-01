package com.rf.jimbe

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// PAKSA IMPORT PROJEK SENDIRI BIAR GAK CRASH NULL POINTER
import com.rf.jimbe.R

class StatisticActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    private lateinit var tvBebanUtama: TextView
    private lateinit var tvTotalReps: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var lineChartView: View
    private lateinit var llHistoryContainer: LinearLayout

    private lateinit var ivHomeNav: ImageView
    private lateinit var ivStatisticNav: ImageView
    private lateinit var ivChatNav: ImageView
    private lateinit var ivProfileNav: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Inisialisasi ID komponen data statistik
        tvBebanUtama = findViewById(R.id.tvBebanUtama)
        tvTotalReps = findViewById(R.id.tvTotalReps)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)
        lineChartView = findViewById(R.id.lineChartView)
        llHistoryContainer = findViewById(R.id.llHistoryContainer)

        // Inisialisasi ID Komponen Navigasi Baru (SINKRON 100% DENGAN XML)
        ivHomeNav = findViewById(R.id.ivHomeNav)
        ivStatisticNav = findViewById(R.id.ivStatisticNav)
        ivChatNav = findViewById(R.id.ivChatNav)
        ivProfileNav = findViewById(R.id.ivProfileNav)

        // Cek apakah ada intent extra dari Trainer yang ingin melihat stat member tertentu,
        // ATAU ada intent extra dari sisi Member (TARGET_UID)
        val memberIdFromIntent = intent.getStringExtra("MEMBER_ID")
        val targetUidFromIntent = intent.getStringExtra("TARGET_UID")
        val memberNameFromIntent = intent.getStringExtra("MEMBER_NAME")

        // Inisialisasi tombol back, subtitle, dan bottom nav untuk penyembunyian kondisional
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        val cardBottomNav = findViewById<View>(R.id.cardBottomNav)
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)

        if (memberIdFromIntent != null) {
            cardBottomNav.visibility = View.GONE
            ivBack.visibility = View.VISIBLE
            ivBack.setOnClickListener {
                finish()
            }
        }

        // Ambil Data Statistik Real-time dari Firebase
        // Jika memberIdFromIntent ada nilainya, pakai itu. Jika tidak, coba TARGET_UID. Jika null semua, pakai currentUser.uid
        val targetUid = memberIdFromIntent ?: targetUidFromIntent ?: currentUser.uid
        listenToWorkoutStats(targetUid)

        // Tarik data profil member untuk menampilkan Nama & BMI di Subtitle secara dinamis
        database.child("members").child(targetUid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val memberName = snapshot.child("nama_lengkap").value?.toString() ?: memberNameFromIntent ?: "Member"
                val bmiData = snapshot.child("bmi_data")

                if (bmiData.exists()) {
                    val tinggi = bmiData.child("tinggi").value?.toString() ?: "-"
                    val berat = bmiData.child("berat").value?.toString() ?: "-"
                    val score = bmiData.child("score").value?.toString() ?: "-"
                    val status = bmiData.child("status").value?.toString() ?: "-"

                    tvSubtitle.text = "Member: $memberName\nTinggi: $tinggi cm | Berat: $berat kg | BMI: $score ($status)"
                } else {
                    tvSubtitle.text = "Member: $memberName\n(Data BMI belum dihitung)"
                }
                tvSubtitle.visibility = View.VISIBLE
            } else if (!memberNameFromIntent.isNullOrEmpty()) {
                tvSubtitle.text = "Member: $memberNameFromIntent"
                tvSubtitle.visibility = View.VISIBLE
            }
        }

        // Opsional: Tampilkan nama member jika sedang dalam mode Trainer
        if (!memberNameFromIntent.isNullOrEmpty()) {
            Toast.makeText(this, "Menampilkan Statistik: $memberNameFromIntent", Toast.LENGTH_SHORT).show()
        }

        // --- SISTEM AKSI 4 NAVIGASI BARU ---
        ivHomeNav.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        ivStatisticNav.setOnClickListener {
            Toast.makeText(this, "Anda sedang di menu Statistik", Toast.LENGTH_SHORT).show()
        }

        ivChatNav.setOnClickListener {
            startActivity(Intent(this, TrainerListActivity::class.java))
            finish()
        }

        ivProfileNav.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun listenToWorkoutStats(uid: String) {
        database.child("members").child(uid).child("daily_stats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    llHistoryContainer.removeAllViews()

                    if (!snapshot.exists()) return

                    val listBeban = mutableListOf<Float>()
                    var lastBeban = 0
                    var lastReps = 0
                    var totalDurasiMenit = 0

                    for (dateSnapshot in snapshot.children) {
                        val tanggal = dateSnapshot.key ?: "Unknown Date"
                        val beban = dateSnapshot.child("beban").value.toString().toIntOrNull() ?: 0
                        val reps = dateSnapshot.child("repetisi").value.toString().toIntOrNull() ?: 0
                        val durasi = dateSnapshot.child("durasi").value.toString().toIntOrNull() ?: 0

                        lastBeban = beban
                        lastReps = reps
                        totalDurasiMenit += durasi
                        listBeban.add(beban.toFloat())

                        // PEMBUATAN CARD HISTORI PROGRAMMATIC
                        val cardHistori = MaterialCardView(this@StatisticActivity).apply {
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(0, 0, 0, 24)
                            layoutParams = params
                            radius = 48f
                            setCardBackgroundColor(Color.WHITE)
                            strokeWidth = 0
                            cardElevation = 0f

                            val layoutKonten = LinearLayout(this@StatisticActivity).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(40, 32, 40, 32)
                            }

                            val tvTanggal = TextView(this@StatisticActivity).apply {
                                text = "📅 Tanggal: $tanggal"
                                textSize = 15f
                                setTypeface(null, Typeface.BOLD)
                                setTextColor(Color.parseColor("#1C2A38"))
                            }

                            val tvDetail = TextView(this@StatisticActivity).apply {
                                text = "🏋️ Beban: $beban kg   •   🔁 Repetisi: $reps Reps   •   ⏱️ Durasi: $durasi Menit"
                                textSize = 13f
                                setTextColor(Color.parseColor("#6A7B8C"))
                                setPadding(0, 10, 0, 0)
                            }

                            layoutKonten.addView(tvTanggal)
                            layoutKonten.addView(tvDetail)
                            addView(layoutKonten)
                        }

                        llHistoryContainer.addView(cardHistori)
                    }

                    tvBebanUtama.text = "$lastBeban kg"
                    tvTotalReps.text = "$lastReps Reps"
                    tvTotalDuration.text = "$totalDurasiMenit Menit"

                    if (lineChartView is LineChartView) {
                        if (listBeban.size >= 2) {
                            (lineChartView as LineChartView).setData(listBeban)
                        } else if (listBeban.size == 1) {
                            (lineChartView as LineChartView).setData(listOf(listBeban[0], listBeban[0]))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@StatisticActivity, "Gagal memuat histori", Toast.LENGTH_SHORT).show()
                }
            })
    }
}