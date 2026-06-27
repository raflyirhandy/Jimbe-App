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

        // Tarik data registrasi awal dari path "members" di Firebase
        database.child("members").child(currentUser.uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val phone = snapshot.child("nomor_hp").value?.toString() ?: "Belum Mengisi"
                val address = snapshot.child("alamat").value?.toString() ?: "Belum Mengisi"

                tvProfilePhone.text = phone
                tvProfileAddress.text = address
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat informasi akun", Toast.LENGTH_SHORT).show()
        }

        // Logika Klik Menu Navigasi Bawah
        ivHomeNav.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        ivStatisticNav.setOnClickListener {
            startActivity(Intent(this, StatisticActivity::class.java))
            finish()
        }
        ivChatNav.setOnClickListener {
            startActivity(Intent(this, TrainerListActivity::class.java))
            finish()
        }

        // Fungsi Tombol Keluar Akun
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_SHORT).show()
        }
    }
}