package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// PAKSA IMPORT PROJEK SENDIRI
import com.rf.jimbe.R

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Inisialisasi ID komponen TextView Read-Only
        val tvProfileTitle = findViewById<TextView>(R.id.tvProfileTitle)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfileUID = findViewById<TextView>(R.id.tvProfileUID)
        val tvProfilePhone = findViewById<TextView>(R.id.tvProfilePhone)
        val tvProfileAddress = findViewById<TextView>(R.id.tvProfileAddress)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        // Hubungkan Navigasi Menu Bawah
        val ivHomeNav = findViewById<ImageView>(R.id.ivHomeNav)
        val ivStatisticNav = findViewById<ImageView>(R.id.ivStatisticNav)
        val ivChatNav = findViewById<ImageView>(R.id.ivChatNav)

        tvProfileEmail.text = currentUser.email
        tvProfileUID.text = currentUser.uid

        var isTrainer = false

        // Tentukan apakah user aktif adalah Trainer atau Member
        database.child("trainers").child(currentUser.uid).get().addOnSuccessListener { snapshotTrainer ->
            if (snapshotTrainer.exists()) {
                isTrainer = true
                tvProfileTitle.text = "Profil Trainer JIMBE"
                val phone = snapshotTrainer.child("nomor_hp").value?.toString() ?: "Belum Mengisi"
                val address = snapshotTrainer.child("alamat").value?.toString() ?: "Belum Mengisi"

                tvProfilePhone.text = phone
                tvProfileAddress.text = address
            } else {
                tvProfileTitle.text = "Profil Member JIMBE"
                database.child("members").child(currentUser.uid).get().addOnSuccessListener { snapshotMember ->
                    if (snapshotMember.exists()) {
                        val phone = snapshotMember.child("nomor_hp").value?.toString() ?: "Belum Mengisi"
                        val address = snapshotMember.child("alamat").value?.toString() ?: "Belum Mengisi"

                        tvProfilePhone.text = phone
                        tvProfileAddress.text = address
                    }
                }
            }
        }.addOnFailureListener {
            tvProfileTitle.text = "Profil Member JIMBE"
            Toast.makeText(this, "Gagal memuat informasi akun", Toast.LENGTH_SHORT).show()
        }

        // Logika Klik Menu Navigasi Bawah
        ivHomeNav.setOnClickListener {
            if (isTrainer) {
                startActivity(Intent(this, TrainerActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }
        ivStatisticNav.setOnClickListener {
            if (isTrainer) {
                startActivity(Intent(this, SelectMemberParamActivity::class.java))
            } else {
                startActivity(Intent(this, StatisticActivity::class.java))
            }
            finish()
        }
        ivChatNav.setOnClickListener {
            if (isTrainer) {
                startActivity(Intent(this, MemberListActivity::class.java))
            } else {
                startActivity(Intent(this, TrainerListActivity::class.java))
            }
            finish()
        }

        // Fungsi Tombol Keluar Akun
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_SHORT).show()
        }
    }
}