package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
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
        val tvTrainerBadge1 = findViewById<TextView>(R.id.tvTrainerBadge1)
        val tvTrainerBadge2 = findViewById<TextView>(R.id.tvTrainerBadge2)

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
                        listenUnreadForTrainer(uid, tvTrainerBadge1)
                    } else if (counter == 2) {
                        trainerId2 = uid
                        trainerName2 = nama
                        tvNamaTrainer2.text = nama
                        listenUnreadForTrainer(uid, tvTrainerBadge2)
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

    private fun listenUnreadForTrainer(trainerUid: String, badgeView: TextView) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatRoomId = if (currentUserId < trainerUid) {
            "${currentUserId}_${trainerUid}"
        } else {
            "${trainerUid}_${currentUserId}"
        }
        
        FirebaseDatabase.getInstance().reference.child("chats").child(chatRoomId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var unreadCount = 0
                    for (messageSnapshot in snapshot.children) {
                        val senderId = messageSnapshot.child("senderId").value?.toString()
                        val read = messageSnapshot.child("read").value as? Boolean ?: false
                        if (senderId != null && senderId != currentUserId && !read) {
                            unreadCount++
                        }
                    }
                    if (unreadCount > 0) {
                        badgeView.text = unreadCount.toString()
                        badgeView.visibility = View.VISIBLE
                    } else {
                        badgeView.visibility = View.GONE
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}