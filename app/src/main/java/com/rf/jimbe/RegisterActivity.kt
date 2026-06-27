package com.rf.jimbe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rf.jimbe.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.tvLogin.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val nama = binding.etNama.text.toString().trim()
            val noHp = binding.etNoHp.text.toString().trim()
            val alamat = binding.etAlamat.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || nama.isEmpty() || noHp.isEmpty() || alamat.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Semua data tidak boleh ada yang kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Daftarkan Akun di Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        if (userId != null) {
                            // MENYUNTIKKAN FIELD ROLE DAN STATUS SESUAI ATURAN SKEMA UAS
                            val memberBaru = hashMapOf(
                                "id" to userId,
                                "nama_lengkap" to nama,
                                "nomor_hp" to noHp,
                                "alamat" to alamat,
                                "gender" to gender,
                                "role" to "member",               // Otomatis diset sebagai member biasa
                                "status_member" to "non-active"    // Menunggu validasi pembayaran/admin
                            )

                            // 2. Simpan Profil ke Realtime Database di bawah node "members/UID"
                            database.child("members").child(userId).setValue(memberBaru)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registrasi Berhasil, silakan Login", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registrasi Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}