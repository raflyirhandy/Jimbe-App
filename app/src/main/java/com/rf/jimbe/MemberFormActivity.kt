package com.rf.jimbe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rf.jimbe.databinding.ActivityMemberFormBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MemberFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemberFormBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val alamat = binding.etAlamat.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || alamat.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 1)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiredDate = dateFormat.format(calendar.time)

            // Menggunakan push() untuk membuat ID acak otomatis di Realtime Database
            val newMemberKey = database.child("members").push().key

            val newMember = hashMapOf(
                "id" to newMemberKey,
                "nama_lengkap" to name,
                "nomor_hp" to phone,
                "alamat" to alamat,
                "gender" to gender,
                "tanggal_berakhir_member" to expiredDate
            )

            if (newMemberKey != null) {
                database.child("members").child(newMemberKey).setValue(newMember)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Berhasil menambah member", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}