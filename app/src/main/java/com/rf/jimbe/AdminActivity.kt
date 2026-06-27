package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.rf.jimbe.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            redirectToLogin()
            return
        }

        // Ambil nama Admin dari Firebase Realtime Database
        FirebaseDatabase.getInstance().reference.child("members").child(currentUser.uid)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val namaAdmin = snapshot.child("nama_lengkap").value.toString()
                    binding.tvGreetingAdmin.text = "Selamat Datang Admin Utama,\n$namaAdmin!"
                }
            }

        binding.cvMemberAdmin.setOnClickListener {
            startActivity(Intent(this, MemberListActivity::class.java))
        }

        binding.cvPaymentAdmin.setOnClickListener {
            Toast.makeText(this, "Fitur Catat Pembayaran belum tersedia", Toast.LENGTH_SHORT).show()
        }

        binding.cvWorkoutAdmin.setOnClickListener {
            startActivity(Intent(this, WorkoutKatalogActivity::class.java))
        }

        binding.cvLogoutAdmin.setOnClickListener {
            auth.signOut()
            redirectToLogin()
            Toast.makeText(this, "Admin Berhasil Logout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}