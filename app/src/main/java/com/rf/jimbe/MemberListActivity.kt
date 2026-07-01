package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rf.jimbe.databinding.ActivityMemberListBinding

class MemberListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemberListBinding

    // Variabel untuk koneksi ke Realtime Database
    private lateinit var database: DatabaseReference
    private var unreadCountsMap = HashMap<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil instance Realtime Database
        database = FirebaseDatabase.getInstance().reference

        binding.rvMembers.layoutManager = LinearLayoutManager(this)

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, MemberFormActivity::class.java))
        }

        listenAllUnreadCounts()
    }

    override fun onResume() {
        super.onResume()
        fetchMembers()
    }

    private fun fetchMembers() {
        // Mengambil seluruh data dari node "members" secara realtime
        database.child("members")
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val membersList = mutableListOf<Member>()

                    if (snapshot.exists()) {
                        for (memberSnapshot in snapshot.children) {

                            // Proteksi aman dari string Kosong / Null Object
                            val rawId = memberSnapshot.child("id").value?.toString() ?: ""
                            val id = rawId.takeIf { it.isNotBlank() } ?: memberSnapshot.key ?: ""

                            val rawNama =
                                memberSnapshot.child("nama_lengkap").value?.toString() ?: ""
                            val nama = rawNama.takeIf { it.isNotBlank() } ?: "Member Baru"

                            val rawPhone = memberSnapshot.child("nomor_hp").value?.toString() ?: ""
                            val phone = rawPhone.takeIf { it.isNotBlank() } ?: "Belum ada nomor"

                            val rawAlamat = memberSnapshot.child("alamat").value?.toString() ?: ""
                            val alamat =
                                rawAlamat.takeIf { it.isNotBlank() } ?: "Alamat belum diatur"

                            val rawGender = memberSnapshot.child("gender").value?.toString() ?: ""
                            val gender = rawGender.takeIf { it.isNotBlank() } ?: "-"

                            val rawExpired =
                                memberSnapshot.child("tanggal_berakhir_member").value?.toString()
                                    ?: ""
                            val expired = rawExpired.takeIf { it.isNotBlank() } ?: "Belum aktif"

                            val member = Member(
                                id = id,
                                nama_lengkap = nama,
                                nomor_hp = phone,
                                alamat = alamat,
                                gender = gender,
                                tanggal_berakhir_member = expired
                            )
                            membersList.add(member)
                        }
                    }

                    // Kirim data ke RecyclerView Adapter beserta lambda click listener
                    val adapter = MemberAdapter(membersList) { selectedMember ->
                        val intent =
                            Intent(this@MemberListActivity, ChatActivity::class.java).apply {
                                putExtra(
                                    "MEMBER_ID",
                                    selectedMember.id.ifEmpty { "default_member" })
                                putExtra("MEMBER_NAME", selectedMember.nama_lengkap)
                            }
                        startActivity(intent)
                    }
                    adapter.updateUnreadCounts(unreadCountsMap)
                    binding.rvMembers.adapter = adapter
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Toast.makeText(
                        this@MemberListActivity,
                        "Gagal mengambil data: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun listenAllUnreadCounts() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child("chats").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                unreadCountsMap.clear()
                for (chatRoomSnapshot in snapshot.children) {
                    val chatRoomId = chatRoomSnapshot.key ?: continue
                    if (chatRoomId.contains(currentUserId)) {
                        val partnerId = chatRoomId.replace(currentUserId, "").replace("_", "")
                        var unread = 0
                        for (messageSnapshot in chatRoomSnapshot.children) {
                            val senderId = messageSnapshot.child("senderId").value?.toString()
                            val read = messageSnapshot.child("read").value as? Boolean ?: false
                            if (senderId != null && senderId != currentUserId && !read) {
                                unread++
                            }
                        }
                        if (unread > 0) {
                            unreadCountsMap[partnerId] = unread
                        }
                    }
                }
                val adapter = binding.rvMembers.adapter as? MemberAdapter
                adapter?.updateUnreadCounts(unreadCountsMap)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }
}
