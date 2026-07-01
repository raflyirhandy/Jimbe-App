package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TrainerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var rvJadwalLatihan: RecyclerView
    private val kelasList = mutableListOf<KelasLatihan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val currentTrainer = auth.currentUser

        val tvWelcomeTrainer = findViewById<TextView>(R.id.tvWelcomeTrainer)
        val btnBuatKelas = findViewById<CardView>(R.id.btnBuatKelas)
        rvJadwalLatihan = findViewById(R.id.rvJadwalLatihan)
        
        // Bottom Nav components
        val ivHomeNav = findViewById<ImageView>(R.id.ivHomeNav)
        val ivStatisticNav = findViewById<ImageView>(R.id.ivStatisticNav)
        val ivChatNav = findViewById<ImageView>(R.id.ivChatNav)
        val ivProfileNav = findViewById<ImageView>(R.id.ivProfileNav)

        val tvChatBadge = findViewById<TextView>(R.id.tvChatBadge)
        if (currentTrainer != null) {
            listenUnreadMessagesCount(currentTrainer.uid, tvChatBadge)
        }

        tvWelcomeTrainer.text = "Selamat Datang Trainer,\n${currentTrainer?.email?.substringBefore("@")?.uppercase() ?: "TRAINER"}!"

        // Konfigurasi RecyclerView
        rvJadwalLatihan.layoutManager = LinearLayoutManager(this)

        // Tombol Buat Jadwal Kelas
        btnBuatKelas.setOnClickListener {
            startActivity(Intent(this, CreateJadwalActivity::class.java))
        }

        // --- SISTEM NAVIGASI ---
        ivHomeNav.setOnClickListener {
            // Already here
        }
        
        ivStatisticNav.setOnClickListener {
            // Membuka daftar member untuk dipantau grafiknya
            startActivity(Intent(this, SelectMemberParamActivity::class.java))
        }
        
        ivChatNav.setOnClickListener {
            // Membuka daftar member untuk chat
            startActivity(Intent(this, MemberListActivity::class.java))
        }
        
        ivProfileNav.setOnClickListener {
            // Buka ProfileActivity (dimana logout sekarang berada)
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun listenUnreadMessagesCount(currentUserId: String, tvChatBadge: TextView) {
        database.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalUnread = 0
                for (chatRoomSnapshot in snapshot.children) {
                    val chatRoomId = chatRoomSnapshot.key ?: continue
                    if (chatRoomId.contains(currentUserId)) {
                        for (messageSnapshot in chatRoomSnapshot.children) {
                            val senderId = messageSnapshot.child("senderId").value?.toString()
                            val read = messageSnapshot.child("read").value as? Boolean ?: false
                            if (senderId != null && senderId != currentUserId && !read) {
                                totalUnread++
                            }
                        }
                    }
                }
                if (totalUnread > 0) {
                    tvChatBadge.text = totalUnread.toString()
                    tvChatBadge.visibility = android.view.View.VISIBLE
                } else {
                    tvChatBadge.visibility = android.view.View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onResume() {
        super.onResume()
        fetchJadwalLatihan()
    }

    private fun fetchJadwalLatihan() {
        database.child("kelas_latihan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kelasList.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val kelas = data.getValue(KelasLatihan::class.java)
                        if (kelas != null) {
                            kelasList.add(kelas)
                        }
                    }
                }
                
                // Set Adapter
                rvJadwalLatihan.adapter = KelasLatihanAdapter(kelasList, isTrainer = true) { selectedKelas ->
                    // Fetch list peserta
                    database.child("kelas_latihan").child(selectedKelas.id).child("peserta")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(pesertaSnapshot: DataSnapshot) {
                                val pesertaNames = mutableListOf<String>()
                                if (pesertaSnapshot.exists()) {
                                    for (child in pesertaSnapshot.children) {
                                        val name = child.getValue(String::class.java) ?: "Member"
                                        pesertaNames.add(name)
                                    }
                                }

                                val listArray = if (pesertaNames.isEmpty()) arrayOf("Belum ada peserta") else pesertaNames.toTypedArray()
                                androidx.appcompat.app.AlertDialog.Builder(this@TrainerActivity)
                                    .setTitle("Daftar Peserta: ${selectedKelas.namaLatihan}")
                                    .setItems(listArray, null)
                                    .setPositiveButton("Tutup", null)
                                    .show()
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@TrainerActivity, "Gagal mengambil data peserta", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TrainerActivity, "Gagal memuat jadwal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}