package com.rf.jimbe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rf.jimbe.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var database: DatabaseReference
    private lateinit var chatAdapter: ChatAdapter
    private var messageList = ArrayList<MessageModel>()

    private var chatRoomId: String = ""
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ambil data Intent secara dinamis (Dua Arah: dari TrainerListActivity atau MemberListActivity)
        val trainerId = intent.getStringExtra("TRAINER_ID")
        val memberId = intent.getStringExtra("MEMBER_ID")

        // Tentukan siapa lawan bicaranya (partner)
        val partnerId = if (!trainerId.isNullOrEmpty()) trainerId else if (!memberId.isNullOrEmpty()) memberId else ""
        val partnerName = intent.getStringExtra("TRAINER_NAME") ?: intent.getStringExtra("MEMBER_NAME") ?: "Chat Room"

        // Set nama lawan bicara di toolbar/header chat
        binding.tvChatTitle.text = partnerName

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance().reference

        // 2. KUNCI RAHASIA: Bikin Chat Room ID Unik (Urutkan ID secara alfabetis agar konsisten dua arah)
        chatRoomId = if (currentUserId < partnerId) {
            "${currentUserId}_${partnerId}"
        } else {
            "${partnerId}_${currentUserId}"
        }

        // Setup RecyclerView Chat
        chatAdapter = ChatAdapter(messageList, currentUserId)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = chatAdapter

        // 3. LISTEN DATA: Mengambil & Memantau Chat secara Realtime
        listenForMessages()

        // 4. KIRIM PESAN: Logika pas tombol kirim ditekan
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }
    }

    private fun listenForMessages() {
        database.child("chats").child(chatRoomId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(MessageModel::class.java)
                        if (message != null) {
                            messageList.add(message)
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                    // Otomatis scroll ke pesan paling bawah
                    if (messageList.isNotEmpty()) {
                        binding.rvChat.smoothScrollToPosition(messageList.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Gagal memuat chat: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendMessage(text: String) {
        val messageId = database.child("chats").child(chatRoomId).push().key ?: return
        val messageMap = MessageModel(
            senderId = currentUserId,
            message = text,
            timestamp = System.currentTimeMillis()
        )

        // Simpan pesan ke node chats -> chatRoomId -> messageId
        database.child("chats").child(chatRoomId).child(messageId).setValue(messageMap)
            .addOnSuccessListener {
                binding.etMessage.setText("") // Kosongkan input boks setelah kirim
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show()
            }
    }
}
