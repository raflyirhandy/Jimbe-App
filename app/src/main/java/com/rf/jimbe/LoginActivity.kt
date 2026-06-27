package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // SINKRONISASI CEK USER AKTIF PADA SAAT APLIKASI DIKLIK
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkUserRoleAndRedirect(currentUser.uid)
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proses autentikasi masuk Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener
                    Toast.makeText(this, "Login Berhasil! Mengecek Hak Akses...", Toast.LENGTH_SHORT).show()
                    checkUserRoleAndRedirect(uid)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal Masuk: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Pindah Halaman ke RegisterActivity jika diklik
        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // ENGINE UTAMA BARU: Deteksi Akurat Bertingkat Berdasarkan Folder Terpisah (Admin -> Trainer -> Member)
    private fun checkUserRoleAndRedirect(uid: String) {
        // 1. KETUK PINTU KE FOLDER ADMINS
        database.child("admins").child(uid).get().addOnSuccessListener { snapshotAdmin ->
            if (snapshotAdmin.exists()) {
                // Jika UID terdaftar di folder admins, lempar ke AdminActivity
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
            } else {

                // 2. JIKA BUKAN ADMIN, KETUK PINTU KE FOLDER TRAINERS
                database.child("trainers").child(uid).get().addOnSuccessListener { snapshotTrainer ->
                    if (snapshotTrainer.exists()) {
                        // Jika UID terdaftar di folder trainers (Ammar), lempar ke TrainerActivity
                        startActivity(Intent(this, TrainerActivity::class.java))
                        finish()
                    } else {

                        // 3. JIKA TIDAK ADA DI ADMINS & TRAINERS, MAKA DIA ADALAH MEMBER MURNI
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }.addOnFailureListener {
                    // Proteksi jika koneksi drop, default lempar ke member
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }.addOnFailureListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}