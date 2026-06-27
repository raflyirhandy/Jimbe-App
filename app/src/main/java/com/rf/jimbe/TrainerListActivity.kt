package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TrainerListActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference.child("trainers")

    // Variabel untuk menyimpan UID dan Nama asli dari Firebase
    private var trainerId1: String = ""
    private var trainerName1: String = ""

    private var trainerId2: String = ""
    private var trainerName2: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_list)

        val cardTrainer1 = findViewById<MaterialCardView>(R.id.cardTrainer1)
        val cardTrainer2 = findViewById<MaterialCardView>(R.id.cardTrainer2)
        val tvNamaTrainer1 = findViewById<TextView>(R.id.tvNamaTrainer1)
        val tvNamaTrainer2 = findViewById<TextView>(R.id.tvNamaTrainer2)

        // AMBIL DATA DARI FIREBASE SECARA REALTIME
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var counter = 1

                for (trainerSnapshot in snapshot.children) {
                    val uid = trainerSnapshot.key ?: ""
                    val nama = trainerSnapshot.child("nama_lengkap").value?.toString() ?: "Trainer"

                    if (counter == 1) {
                        trainerId1 = uid
                        trainerName1 = nama
                        tvNamaTrainer1.text = nama
                    } else if (counter == 2) {
                        trainerId2 = uid
                        trainerName2 = nama
                        tvNamaTrainer2.text = nama
                        break // Batasi hanya mengambil 2 data teratas
                    }
                    counter++
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TrainerListActivity, "Gagal memuat: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // KLIK CARD LANGSUNG MEMBAWA UID DAN NAMA ASLI DARI FIREBASE
        cardTrainer1.setOnClickListener {
            if (trainerId1.isNotEmpty()) {
                openChatRoom(trainerId1, trainerName1)
            } else {
                Toast.makeText(this, "Data trainer belum siap", Toast.LENGTH_SHORT).show()
            }
        }

        cardTrainer2.setOnClickListener {
            if (trainerId2.isNotEmpty()) {
                openChatRoom(trainerId2, trainerName2)
            } else {
                Toast.makeText(this, "Data trainer belum siap", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openChatRoom(trainerId: String, trainerName: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("TRAINER_ID", trainerId)
            putExtra("TRAINER_NAME", trainerName)
        }
        startActivity(intent)
    }
}